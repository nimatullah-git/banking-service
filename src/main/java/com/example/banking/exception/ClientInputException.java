package com.example.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author nimatullah
 */

// Общее исключение для ошибок ввода данных пользователя
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ClientInputException extends RuntimeException {
    public ClientInputException(String message) {
        super(message);
    }
}
