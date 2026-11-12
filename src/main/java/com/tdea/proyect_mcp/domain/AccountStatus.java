package com.tdea.proyect_mcp.domain;

/** Lifecycle state of a demo bank account. */
public enum AccountStatus {
    /** The account can perform banking operations. */
    ACTIVE,
    /** The account is temporarily prevented from performing operations. */
    BLOCKED,
    /** The account is permanently closed. */
    CLOSED
}
