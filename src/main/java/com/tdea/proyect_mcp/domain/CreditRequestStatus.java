package com.tdea.proyect_mcp.domain;

/** Review state of a credit request. Requests are created as pending. */
public enum CreditRequestStatus {
    /** The request has not yet been reviewed. */
    PENDING,
    /** The request was approved by a reviewer. */
    APPROVED,
    /** The request was declined by a reviewer. */
    REJECTED
}
