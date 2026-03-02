package com.jpa.retry_config;

import com.jpa.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

import java.util.Optional;

@Slf4j
public class LoggingRetryListener implements RetryListener {
    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        return true;
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {

    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable e) {
        String apiName = Optional.ofNullable((String) context.getAttribute("api-name"))
                .orElse("default");
//        String lmid = (String) context.getAttribute("lmid");
        if (e != null) {
            log.error("activity={} onError exception={} {}", apiName, e.getClass().getSimpleName(), CommonUtils.getErrorTraceMessage(e));
            return;
        }
        log.info("Retry attempt {} for {}", context.getRetryCount(), apiName);
    }
}
