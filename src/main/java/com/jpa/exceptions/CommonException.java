package com.jpa.exceptions;

import lombok.Getter;

@Getter
public class CommonException extends RuntimeException {
    private final String status;

    public CommonException(String message) {
        super(message);
        this.status = "0600";
    }

}
