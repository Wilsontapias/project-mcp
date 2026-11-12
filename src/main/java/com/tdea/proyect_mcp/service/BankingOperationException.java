package com.tdea.proyect_mcp.service;

/**
 * Signals a rejected banking operation with a message suitable for the local MCP client.
 */
public class BankingOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an operation exception.
     *
     * @param message concise reason the operation was rejected
     */
    public BankingOperationException(String message) {
        super(message);
    }
}
