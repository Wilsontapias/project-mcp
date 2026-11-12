package com.tdea.proyect_mcp.service.dto;

import com.tdea.proyect_mcp.domain.TransactionType;

import java.math.BigDecimal;
/**
 * Safe, serializable summary of a deposited or transferred amount.
 *
 * @param reference unique transaction reference
 * @param type transaction type
 * @param sourceAccountNumber source account, or {@code null} for deposits
 * @param destinationAccountNumber account receiving the amount
 * @param amount transaction amount
 * @param description transaction description
 * @param occurredAt transaction timestamp in ISO-8601 format
 */
public record TransactionSummary(
        String reference,
        TransactionType type,
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal amount,
        String description,
        String occurredAt) {
}
