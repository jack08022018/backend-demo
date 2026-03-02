package com.jpa.saga_config;

@FunctionalInterface
public interface ThrowingFunction2<T, T1, T2, R> {
    R apply(T t, T1 t1, T2 t2) throws Exception;
}
