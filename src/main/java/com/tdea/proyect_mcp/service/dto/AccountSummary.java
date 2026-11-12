package com.tdea.proyect_mcp.service.dto;

import com.tdea.proyect_mcp.domain.AccountStatus;

import java.math.BigDecimal;
/**
 * Safe, serializable account view returned by the banking service and MCP tools.
 *
 * @param accountNumber unique account number
 * @param holderName account holder's display name
 * @param balance current account balance
 * @param status current account status
 * @param createdAt account creation timestamp in ISO-8601 format
 */
public record AccountSummary(
        String accountNumber,
        String holderName,
        BigDecimal balance,
        AccountStatus status,
        String createdAt) {
}
