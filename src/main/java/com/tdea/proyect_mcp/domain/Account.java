package com.tdea.proyect_mcp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/** Persistent account used by the local banking demonstration. */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @NotBlank
    @Size(max = 120)
    @Column(name = "holder_name", nullable = false, length = 120)
    private String holderName;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 17, fraction = 2)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AccountStatus status = AccountStatus.ACTIVE;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Creates the entity instance required by JPA. */
    protected Account() {
        // Required by JPA.
    }

    /**
     * Creates an account model.
     *
     * @param accountNumber unique account number
     * @param holderName account holder's display name
     * @param balance initial balance
     */
    public Account(String accountNumber, String holderName, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
        this.status = AccountStatus.ACTIVE;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /** @return database identifier */
    public Long getId() {
        return id;
    }

    /** @return unique account number */
    public String getAccountNumber() {
        return accountNumber;
    }

    /** @return account holder's display name */
    public String getHolderName() {
        return holderName;
    }

    /** @return current balance */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Updates the balance within a transactional domain operation.
     *
     * @param balance new balance
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /** @return current account status */
    public AccountStatus getStatus() {
        return status;
    }

    /**
     * Changes the account status.
     *
     * @param status new status
     */
    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    /** @return creation timestamp in UTC */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
