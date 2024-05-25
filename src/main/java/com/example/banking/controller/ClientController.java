package com.example.banking.controller;

import com.example.banking.dto.ClientDTO;
import com.example.banking.dto.ClientResponseDTO;
import com.example.banking.dto.ContactInfoDTO;
import com.example.banking.dto.DeleteContactDTO;
import com.example.banking.model.ClientAuthenticationRequest;
import com.example.banking.service.AuthenticationService;
import com.example.banking.service.ClientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * @author nimatullah
 */

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private final ClientService clientService;
    private final AuthenticationService authenticationService;

    @Autowired
    public ClientController(ClientService clientService, AuthenticationService authenticationService) {
        this.clientService = clientService;
        this.authenticationService = authenticationService;
    }

    // Создание клиента
    @PostMapping("/register")
    public ResponseEntity<?> createClient(@Valid @RequestBody ClientDTO clientDTO) {
        try {
            clientService.createClient(clientDTO);
            logger.info("Client created successfully!");
            return new ResponseEntity<>("Client created successfully!", HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating client", e);
            return new ResponseEntity<>("Error creating client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@Valid @RequestBody final ClientAuthenticationRequest clientAuthenticationRequest) {
        return authenticationService.authenticate(clientAuthenticationRequest);
    }


    // Обновление контактной информации клиента
    @PutMapping("/{clientId}/contact")
    @PreAuthorize("@jwtUserDetailsService.preAuthorizeClient()")
    public ResponseEntity<?> updateClientContactInfo(@PathVariable Long clientId, @Valid @RequestBody ContactInfoDTO contactInfoDTO) {
        ClientResponseDTO updatedClient = clientService.updateClientContactInfo(clientId, contactInfoDTO.getPhoneNumber(), contactInfoDTO.getEmail());
        return ResponseEntity.ok(updatedClient);
    }

    // Удаление контактной информации клиента
    @DeleteMapping("/{clientId}/contact")
    @PreAuthorize("@jwtUserDetailsService.preAuthorizeClient()")
    public ResponseEntity<?> deleteClientContactInfo(@PathVariable("clientId") Long clientId, @RequestBody DeleteContactDTO deleteContactDTO) {
        ClientResponseDTO client = clientService.deleteClientContactInfo(clientId, deleteContactDTO.isDeletePhoneNumber(), deleteContactDTO.isDeleteEmail());
        return ResponseEntity.ok(client);
    }

    // Поиск клиентов
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> searchClients(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        Page<ClientResponseDTO> clients = clientService.searchClients(fullName, phoneNumber, email, birthDate, page, size, sortBy);
        return ResponseEntity.ok(clients.getContent());
    }
}
