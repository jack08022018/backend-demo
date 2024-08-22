package com.jpa.utils;

@FunctionalInterface
public interface ExcuteApi<T> {
    T apply() throws Exception;
}
