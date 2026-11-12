package com.tdea.proyect_mcp.service.dto;

import com.tdea.proyect_mcp.domain.CreditRequestStatus;

import java.math.BigDecimal;
/**
 * Safe, serializable summary of a credit application.
 *
 * @param id database identifier
 * @param accountNumber applicant's account number
 * @param requestedAmount requested amount
 * @param purpose request purpose
 * @param status review status
 * @param requestedAt request timestamp in ISO-8601 format
 */
public record CreditRequestSummary(
        Long id,
        String accountNumber,
        BigDecimal requestedAmount,
        String purpose,
        CreditRequestStatus status,
        String requestedAt) {
}
