package com.tdea.proyect_mcp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/** A credit application submitted against an account. */
@Entity
@Table(name = "credit_requests")
public class CreditRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 17, fraction = 2)
    @Column(name = "requested_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedAmount;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String purpose;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CreditRequestStatus status = CreditRequestStatus.PENDING;

    @NotNull
    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    /** Creates the entity instance required by JPA. */
    protected CreditRequest() {
        // Required by JPA.
    }

    /**
     * Creates a pending credit request.
     *
     * @param account account requesting credit
     * @param requestedAmount positive amount requested
     * @param purpose reason for the request
     */
    public CreditRequest(Account account, BigDecimal requestedAmount, String purpose) {
        this.account = account;
        this.requestedAmount = requestedAmount;
        this.purpose = purpose;
        this.status = CreditRequestStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        if (requestedAt == null) {
            requestedAt = Instant.now();
        }
    }

    /** @return database identifier */
    public Long getId() {
        return id;
    }

    /** @return account associated with the request */
    public Account getAccount() {
        return account;
    }

    /** @return requested amount */
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    /** @return purpose supplied by the applicant */
    public String getPurpose() {
        return purpose;
    }

    /** @return current review status */
    public CreditRequestStatus getStatus() {
        return status;
    }

    /**
     * Updates the review status.
     *
     * @param status new status
     */
    public void setStatus(CreditRequestStatus status) {
        this.status = status;
    }

    /** @return request timestamp in UTC */
    public Instant getRequestedAt() {
        return requestedAt;
    }
}
