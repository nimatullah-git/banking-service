package com.example.banking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author nimatullah
 */

@Data
public class ContactInfoDTO {
    @NotBlank
    private String phoneNumber;

    @NotBlank
    @Email
    private String email;
}
