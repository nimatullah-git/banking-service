package com.example.banking.repository;

import com.example.banking.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author nimatullah
 */

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByUsername(String login);

    Optional<Client> findOneByPhoneNumber(String phoneNumber);

    // Примечание: Использование "One" в методе findOneByPhoneNumber для предотвращения конфликта с Swagger во время выполнения.
    Optional<Client> findOneByEmail(String email);

    Page<Client> findByFullNameLike(String fullName, Pageable pageable);

    Page<Client> findByPhoneNumber(String phoneNumber, Pageable pageable);

    Page<Client> findByEmail(String email, Pageable pageable);

    Page<Client> findByBirthDateAfter(String birthDate, Pageable pageable);

}
