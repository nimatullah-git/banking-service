package com.example.banking.repository;

import com.example.banking.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author nimatullah
 */

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
