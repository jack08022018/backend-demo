package com.jpa.saga_config;

@FunctionalInterface
public interface ThrowingFunction1<T, T1, R> {
    R apply(T t, T1 t1) throws Exception;
}
