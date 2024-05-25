package com.example.banking.repository;

import com.example.banking.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author nimatullah
 */
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByClientId(Long clientId);
}
