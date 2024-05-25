package com.example.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author nimatullah
 */

// Общее исключение для ошибок баланса счета
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BalanceException extends RuntimeException {
    public BalanceException(String message) {
        super(message);
    }
}
