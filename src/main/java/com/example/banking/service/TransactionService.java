package com.example.banking.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.banking.dto.TransactionDTO;
import com.example.banking.exception.BalanceException;
import com.example.banking.exception.UserNotFoundException;
import com.example.banking.model.BankAccount;
import com.example.banking.model.Transaction;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Сервис для обработки транзакций между банковскими счетами.
 *
 * @author nimatullah
 */
@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, BankAccountRepository bankAccountRepository) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    /**
     * Метод для перевода средств между счетами.
     *
     * @param transactionDTO данные транзакции.
     * @return ResponseEntity с сообщением об успешной транзакции и ID транзакции.
     */
    @Transactional
    @PreAuthorize("@jwtUserDetailsService.preAuthorizeClient()")
    public ResponseEntity<String> transfer(TransactionDTO transactionDTO) {
        logger.info("Starting transfer from client ID {} to client ID {}", transactionDTO.getFromClientId(), transactionDTO.getToClientId());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Invalid or expired token");
            throw new JWTVerificationException("Invalid or expired token");
        }

        if (transactionDTO.getFromClientId().equals(transactionDTO.getToClientId())) {
            logger.warn("Cannot transfer money to the same account");
            throw new UserNotFoundException("Cannot transfer money to the same account");
        }

        BankAccount fromAccount = bankAccountRepository.findById(transactionDTO.getFromClientId())
                .orElseThrow(() -> {
                    logger.warn("Invalid user ID for fromAccount: {}", transactionDTO.getFromClientId());
                    return new UserNotFoundException("Invalid user ID");
                });

        BankAccount toAccount = bankAccountRepository.findById(transactionDTO.getToClientId())
                .orElseThrow(() -> {
                    logger.warn("Invalid user ID for toAccount: {}", transactionDTO.getToClientId());
                    return new UserNotFoundException("Invalid user ID");
                });

        if (fromAccount.getBalance().compareTo(transactionDTO.getAmount()) < 0) {
            logger.warn("Insufficient balance for account ID {}", fromAccount.getId());
            throw new BalanceException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(transactionDTO.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transactionDTO.getAmount()));

        logger.info("Saving updated balances for accounts ID {} and ID {}", fromAccount.getId(), toAccount.getId());
        bankAccountRepository.save(fromAccount);
        bankAccountRepository.save(toAccount);

        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);

        logger.info("Transaction successful! Transaction ID: {}", transaction.getId());
        return ResponseEntity.ok("Transaction successful! Transaction ID: " + transaction.getId());
    }
}
