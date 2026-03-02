package com.jpa.saga_config;

import com.jpa.exceptions.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.function.ThrowingConsumer;
import org.springframework.util.function.ThrowingFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class SagaConfig {
    private final Options options;
    private final List<Callable<?>> compensationOps = new ArrayList<>();

    public static final class Options {
        private final Boolean parallelCompensation;
        private final Boolean continueWithError;

        private Options(boolean parallelCompensation, boolean continueWithError) {
            this.parallelCompensation = parallelCompensation;
            this.continueWithError = continueWithError;
        }

        public static final class Builder {
            private Boolean parallelCompensation;
            private Boolean continueWithError;

            public Builder setParallelCompensation(boolean parallelCompensation) {
                this.parallelCompensation = parallelCompensation;
                return this;
            }

            public Builder setContinueWithError(boolean continueWithError) {
                this.continueWithError = continueWithError;
                return this;
            }

            public Options build() {
                parallelCompensation = parallelCompensation == null || parallelCompensation;
                continueWithError = continueWithError == null || continueWithError;
                return new Options(parallelCompensation, continueWithError);
            }
        }
    }

    public SagaConfig(Options options) {
        this.options = options;
    }

    public <T, R> void addCompensation(ThrowingFunction<T, R> operation, T dto) {
        Callable<R> task = () -> operation.apply(dto);
        compensationOps.add(task);
    }

    public <T, T1, R> void addCompensation(ThrowingFunction1<T, T1, R> operation, T t, T1 t1) {
        Callable<R> task = () -> operation.apply(t, t1);
        compensationOps.add(task);
    }

    public <T, T1, T2, R> void addCompensation(ThrowingFunction2<T, T1, T2, R> operation, T t, T1 t1, T2 t2) {
        Callable<R> task = () -> operation.apply(t, t1, t2);
        compensationOps.add(task);
    }

    public <T> void addCompensation(ThrowingConsumer<T> operation, T dto) {
        Callable<Void> task = () -> {
            operation.accept(dto);
            return null;
        };
        compensationOps.add(task);
    }

    public static class CompensationException extends RuntimeException {
        public CompensationException(Throwable cause) {
            super("Exception from saga compensate", cause);
        }
    }

    public void compensate() {
        if(CollectionUtils.isEmpty(compensationOps)) {
            return;
        }
        if (Boolean.TRUE.equals(options.parallelCompensation)) {
            executeInParallel();
        } else {
            executeSequentially();
        }
    }

    private void executeInParallel() {
        List<CompletableFuture<?>> futures = createFutures();
        handleParallelResults(futures);
    }

    private void executeSequentially() {
        for (Callable<?> task : compensationOps) {
            executeTaskSequentially(task);
        }
    }

    private List<CompletableFuture<?>> createFutures() {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Callable<?> task : compensationOps) {
            futures.add(CompletableFuture.supplyAsync(() -> callTask(task)));
        }
        return futures;
    }

    private Object callTask(Callable<?> task) {
        try {
            return task.call();
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    private void handleParallelResults(List<CompletableFuture<?>> futures) throws CompensationException {
        CompensationException sagaException = null;
        for (CompletableFuture<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                sagaException = addExceptionToSaga(sagaException, e);
            } catch (InterruptedException e) {
                throw new CompensationException(e);
            }
        }
        if (sagaException != null) {
            throw sagaException;
        }
    }

    private CompensationException addExceptionToSaga(CompensationException sagaException, Exception e) {
        if (sagaException == null) {
            sagaException = new CompensationException(e);
        } else {
            sagaException.addSuppressed(e);
        }
        return sagaException;
    }

    private void executeTaskSequentially(Callable<?> task) {
        try {
            task.call();
        } catch (Exception e) {
            if (Boolean.FALSE.equals(options.continueWithError)) {
                throw new CompensationException(e);
            }
        }
    }
}
