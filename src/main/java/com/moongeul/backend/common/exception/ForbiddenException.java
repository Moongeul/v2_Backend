package com.moongeul.backend.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
    public ForbiddenException() {
        super(HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

    public ForbiddenException(String message, Object data) {
        super(HttpStatus.FORBIDDEN, message, data);
    }
}
