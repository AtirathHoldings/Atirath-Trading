package com.atirath.trading.common;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends ApiException {
    public AccountLockedException(String message) {
        super(message, HttpStatus.LOCKED);
    }
}
