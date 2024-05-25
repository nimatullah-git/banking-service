package com.example.banking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author nimatullah
 */

@Data
public class TransactionDTO {
    @NotNull
    private Long fromClientId;

    @NotNull
    private Long toClientId;

    @NotNull
    @Positive
    private BigDecimal amount;
}
