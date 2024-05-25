package com.example.banking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author nimatullah
 */

@Data
public class ClientDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotNull
    @Positive
    private BigDecimal initialBalance;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    @Email
    private String email;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotBlank
    private String fullName;
}
