package com.example.banking.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Модель запроса аутентификации клиента.
 *
 * @author nimatullah
 */

@Data
public class ClientAuthenticationRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
