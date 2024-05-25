package com.example.banking.controller;

import com.example.banking.dto.TransactionDTO;
import com.example.banking.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nimatullah
 */

@RestController
@RequestMapping("api/transactions")
public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Создание транзакции
    @PostMapping
    @PreAuthorize("@jwtUserDetailsService.preAuthorizeClient()")
    public ResponseEntity<String> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        try {
            return transactionService.transfer(transactionDTO);
        } catch (Exception e) {
            logger.error("Error creating transaction", e);
            return new ResponseEntity<>("Error creating transaction", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
