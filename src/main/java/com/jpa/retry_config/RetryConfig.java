package com.jpa.retry_config;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@EnableRetry
public class RetryConfig {

    @Value("${saga.retry.max-attemps:3}")
    private int maxAttemptsDefault;

    @Value("${saga.retry.initial-interval:1000}")
    private int initialInterval;

    @Value("${saga.retry.multiplier:2}")
    private double multiplier;

    public RetryTemplate getRetryTemplate(Class<? extends Throwable>[] retryFor, int maxAttempts) {
        Set<Class<? extends Throwable>> retryableException = new HashSet<>();
        retryableException.add(StatusRuntimeException.class);
        retryableException.addAll(Arrays.asList(retryFor));
        var template = new RetryTemplate();
        var retryPolicy = new ApiSpecificRetryPolicy(maxAttempts <= 0 ? maxAttemptsDefault : maxAttempts, retryableException);
        template.setRetryPolicy(retryPolicy);

        var backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(multiplier);
//        backOffPolicy.setMaxInterval();
        template.setBackOffPolicy(backOffPolicy);

        template.setListeners(new RetryListener[]{new LoggingRetryListener()});

        return template;
    }

    @Slf4j
    private static class ApiSpecificRetryPolicy extends SimpleRetryPolicy {
        private final int maxAttempts;
        private final Set<Class<? extends Throwable>> retryableException;

        public ApiSpecificRetryPolicy(int maxAttempts, Set<Class<? extends Throwable>> retryableException) {
            this.maxAttempts = maxAttempts;
            this.retryableException = retryableException;
        }

        @Override
        public boolean canRetry(RetryContext context) {
//            String apiName = (String) context.getAttribute("apiName");
//            if (apiName == null) {
//                apiName = "default";
//            }
            Throwable e = context.getLastThrowable();
            if (e != null) {
                boolean isRetryable = retryableException.stream()
                        .anyMatch(s -> s.isAssignableFrom(e.getClass()));
                if (!isRetryable) {
                    return false;
                }
            }
//            int maxAttempts = apiMaxAttempts.getOrDefault(apiName, super.getMaxAttempts());
            return context.getRetryCount() < maxAttempts;
        }
    }

}
