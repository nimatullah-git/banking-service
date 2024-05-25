package com.example.banking.service;

import com.example.banking.exception.BalanceException;
import com.example.banking.model.BankAccount;
import com.example.banking.model.Client;
import com.example.banking.repository.BankAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Сервис для управления банковскими счетами.
 *
 * @author nimatullah
 */
@Service
public class BankAccountService {

    private static final Logger logger = LoggerFactory.getLogger(BankAccountService.class);
    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public BankAccountService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    /**
     * Создает новый банковский счет для клиента с начальным балансом.
     *
     * @param client         клиент, для которого создается счет.
     * @param initialBalance начальный баланс счета.
     */
    public void createBankAccount(Client client, BigDecimal initialBalance) {
        logger.info("Creating bank account for client: {}", client.getUsername());
        BankAccount bankAccount = new BankAccount();
        bankAccount.setClient(client);
        bankAccount.setBalance(initialBalance);
        bankAccount.setInitialBalance(initialBalance);
        bankAccountRepository.save(bankAccount);
        logger.info("Bank account created for client: {}", client.getUsername());
    }

    /**
     * Обновляет баланс всех банковских счетов с фиксированным интервалом.
     * <p>
     * Запускается каждые 60 секунд.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateBalance() {
        logger.info("Starting scheduled balance update.");
        List<BankAccount> accounts = bankAccountRepository.findAll();
        for (BankAccount account : accounts) {
            BigDecimal maxBalance = account.getInitialBalance().multiply(BigDecimal.valueOf(2.07));
            BigDecimal newBalance = account.getBalance().multiply(BigDecimal.valueOf(1.05));

            if (newBalance.compareTo(maxBalance) > 0) {
                logger.error("Max balance exceeded for account ID: {}", account.getId());
                throw new BalanceException("The maximum balance limit has been exceeded for account ID: " + account.getId());
            }

            account.setBalance(newBalance);
            logger.info("Updated balance for account ID: {}", account.getId());
        }
        bankAccountRepository.saveAll(accounts);
        logger.info("Scheduled balance update completed.");
    }
}
