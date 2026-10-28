package com.atirath.trading.common;

import org.springframework.http.HttpStatus;

public class OtpExpiredException extends ApiException {
    public OtpExpiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
