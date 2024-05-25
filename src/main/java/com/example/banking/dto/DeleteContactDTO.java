package com.example.banking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author nimatullah
 */

@Data
public class DeleteContactDTO {
    @NotNull
    private boolean deletePhoneNumber;

    @NotNull
    private boolean deleteEmail;
}
