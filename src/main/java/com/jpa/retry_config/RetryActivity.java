package com.jpa.retry_config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RetryActivity {
    String name();
    Class<? extends Throwable>[] retryFor() default {};
    int maxAttempts() default -1;
}
