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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Immutable audit record for a deposit or transfer. */
@Entity
@Table(name = "bank_transactions")
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String reference;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16, updatable = false)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_account_id", nullable = false)
    private Account destinationAccount;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 17, fraction = 2)
    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Size(max = 255)
    @Column(length = 255, updatable = false)
    private String description;

    @NotNull
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    /** Creates the entity instance required by JPA. */
    protected BankTransaction() {
        // Required by JPA.
    }

    /**
     * Creates a transaction record. The reference and timestamp are assigned before persistence.
     *
     * @param type transaction type
     * @param sourceAccount source account, or {@code null} for a deposit
     * @param destinationAccount account receiving the funds
     * @param amount positive transaction amount
     * @param description human-readable transaction description
     */
    public BankTransaction(
            TransactionType type,
            Account sourceAccount,
            Account destinationAccount,
            BigDecimal amount,
            String description) {
        this.type = type;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        if (reference == null) {
            reference = UUID.randomUUID().toString();
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    /** @return database identifier */
    public Long getId() {
        return id;
    }

    /** @return unique transaction reference */
    public String getReference() {
        return reference;
    }

    /** @return type of the transaction */
    public TransactionType getType() {
        return type;
    }

    /** @return source account, or {@code null} for a deposit */
    public Account getSourceAccount() {
        return sourceAccount;
    }

    /** @return destination account */
    public Account getDestinationAccount() {
        return destinationAccount;
    }

    /** @return amount moved */
    public BigDecimal getAmount() {
        return amount;
    }

    /** @return optional transaction description */
    public String getDescription() {
        return description;
    }

    /** @return transaction timestamp in UTC */
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
