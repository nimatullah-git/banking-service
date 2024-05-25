package com.example.banking.service;

import com.example.banking.dto.ClientDTO;
import com.example.banking.dto.ClientResponseDTO;
import com.example.banking.exception.ClientInputException;
import com.example.banking.exception.UserNotFoundException;
import com.example.banking.model.Client;
import com.example.banking.repository.ClientRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Сервис для управления клиентами.
 *
 * @author nimatullah
 */
@Service
@Transactional
public class ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;
    private final BankAccountService bankAccountService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ClientService(ClientRepository clientRepository, ModelMapper modelMapper, BankAccountService bankAccountService, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.modelMapper = modelMapper;
        this.bankAccountService = bankAccountService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Создает нового клиента и банковский счет для него.
     *
     * @param clientDTO данные клиента для создания.
     */
    public void createClient(ClientDTO clientDTO) {
        logger.info("Creating client: {}", clientDTO.getUsername());
        Client client = clientMapper(clientDTO);
        validateClient(client);
        client.setPassword(passwordEncoder.encode(client.getPassword()));
        clientRepository.save(client);
        bankAccountService.createBankAccount(client, clientDTO.getInitialBalance());
        logger.info("Client created: {}", clientDTO.getUsername());
    }

    /**
     * Обновляет контактную информацию клиента.
     *
     * @param clientId    ID клиента.
     * @param phoneNumber новый номер телефона.
     * @param email       новая электронная почта.
     * @return обновленные данные клиента.
     */
    public ClientResponseDTO updateClientContactInfo(Long clientId, String phoneNumber, String email) {
        logger.info("Updating contact info for client ID: {}", clientId);
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (phoneNumber != null && clientRepository.findOneByPhoneNumber(phoneNumber).isPresent()) {
            logger.warn("Phone number {} already in use", phoneNumber);
            throw new ClientInputException("Phone already in use");
        }

        if (email != null && clientRepository.findOneByEmail(email).isPresent()) {
            logger.warn("Email {} already in use", email);
            throw new ClientInputException("Email already in use");
        }

        client.setPhoneNumber(phoneNumber);
        client.setEmail(email);

        ClientResponseDTO updatedClient = clientMapper(clientRepository.save(client));
        logger.info("Contact info updated for client ID: {}", clientId);
        return updatedClient;
    }

    /**
     * Удаляет контактную информацию клиента.
     *
     * @param clientId          ID клиента.
     * @param deletePhoneNumber флаг удаления номера телефона.
     * @param deleteEmail       флаг удаления электронной почты.
     * @return обновленные данные клиента.
     */
    public ClientResponseDTO deleteClientContactInfo(Long clientId, boolean deletePhoneNumber, boolean deleteEmail) {
        logger.info("Deleting contact info for client ID: {}", clientId);
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (deletePhoneNumber && deleteEmail) {
            logger.warn("Attempt to delete both phone number and email for client ID: {}", clientId);
            throw new ClientInputException("Cannot delete both phone and email. At least one contact method must remain.");
        }

        if (deletePhoneNumber) {
            if (client.getEmail() == null) {
                logger.warn("Attempt to delete phone number with no remaining email for client ID: {}", clientId);
                throw new ClientInputException("Cannot delete phone number. At least one contact method must remain.");
            }
            client.setPhoneNumber(null);
        }

        if (deleteEmail) {
            if (client.getPhoneNumber() == null) {
                logger.warn("Attempt to delete email with no remaining phone number for client ID: {}", clientId);
                throw new ClientInputException("Cannot delete phone number. At least one contact method must remain.");
            }
            client.setEmail(null);
        }

        ClientResponseDTO updatedClient = clientMapper(clientRepository.save(client));
        logger.info("Contact info deleted for client ID: {}", clientId);
        return updatedClient;
    }

    /**
     * Поиск клиентов по различным критериям.
     *
     * @param fullName    полное имя клиента.
     * @param phoneNumber номер телефона клиента.
     * @param email       электронная почта клиента.
     * @param birthDate   дата рождения клиента.
     * @param page        номер страницы.
     * @param size        размер страницы.
     * @param sortBy      параметр сортировки.
     * @return страница с данными клиентов.
     */
    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> searchClients(String fullName, String phoneNumber, String email, LocalDate birthDate, int page, int size, String sortBy) {
        logger.info("Searching clients with criteria - fullName: {}, phoneNumber: {}, email: {}, birthDate: {}", fullName, phoneNumber, email, birthDate);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Client> clients;

        if (fullName != null) {
            clients = clientRepository.findByFullNameLike(fullName, pageable);
        } else if (phoneNumber != null) {
            clients = clientRepository.findByPhoneNumber(phoneNumber, pageable);
        } else if (email != null) {
            clients = clientRepository.findByEmail(email, pageable);
        } else if (birthDate != null) {
            clients = clientRepository.findByBirthDateAfter(String.valueOf(birthDate), pageable);
        } else {
            clients = clientRepository.findAll(pageable);
        }

        logger.info("Found {} clients matching the criteria", clients.getTotalElements());
        return clients.map(this::clientMapper);
    }

    // Вспомогательные методы

    private Client clientMapper(ClientDTO clientDTO) {
        return modelMapper.map(clientDTO, Client.class);
    }

    private ClientResponseDTO clientMapper(Client client) {
        return modelMapper.map(client, ClientResponseDTO.class);
    }

    private void validateClient(Client client) {
        if (clientRepository.findByUsername(client.getUsername()).isPresent()) {
            logger.warn("Username {} already in use", client.getUsername());
            throw new ClientInputException("Login already in use");
        }

        if (clientRepository.findOneByPhoneNumber(client.getPhoneNumber()).isPresent()) {
            logger.warn("Phone number {} already in use", client.getPhoneNumber());
            throw new ClientInputException("Phone number already in use");
        }

        if (clientRepository.findOneByEmail(client.getEmail()).isPresent()) {
            logger.warn("Email {} already in use", client.getEmail());
            throw new ClientInputException("Email already in use");
        }
    }
}
