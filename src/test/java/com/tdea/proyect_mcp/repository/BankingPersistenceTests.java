package com.tdea.proyect_mcp.repository;

import com.tdea.proyect_mcp.domain.Account;
import com.tdea.proyect_mcp.domain.BankTransaction;
import com.tdea.proyect_mcp.domain.CreditRequest;
import com.tdea.proyect_mcp.domain.CreditRequestStatus;
import com.tdea.proyect_mcp.domain.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BankingPersistenceTests {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    @Autowired
    private CreditRequestRepository creditRequestRepository;

    @Test
    void persistsAccountsAndFindsAccountByNumber() {
        Account account = accountRepository.save(
                new Account("10000001", "Ada Lovelace", new BigDecimal("250.00")));

        Account found = accountRepository.findByAccountNumber("10000001").orElseThrow();

        assertThat(found.getId()).isEqualTo(account.getId());
        assertThat(found.getBalance()).isEqualByComparingTo("250.00");
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void persistsTransferAndCreditRequestWithTheirAccountRelationships() {
        Account source = accountRepository.save(
                new Account("10000002", "Grace Hopper", new BigDecimal("500.00")));
        Account destination = accountRepository.save(
                new Account("10000003", "Katherine Johnson", new BigDecimal("100.00")));

        BankTransaction transfer = bankTransactionRepository.save(new BankTransaction(
                TransactionType.TRANSFER,
                source,
                destination,
                new BigDecimal("75.25"),
                "Transferencia de prueba"));

        CreditRequest creditRequest = creditRequestRepository.save(new CreditRequest(
                source,
                new BigDecimal("1000.00"),
                "Crédito educativo"));

        BankTransaction savedTransfer = bankTransactionRepository
                .findByReference(transfer.getReference()).orElseThrow();
        CreditRequest savedRequest = creditRequestRepository
                .findByAccountAccountNumberOrderByRequestedAtDesc("10000002")
                .get(0);

        assertThat(savedTransfer.getType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(savedTransfer.getSourceAccount().getAccountNumber()).isEqualTo("10000002");
        assertThat(savedTransfer.getDestinationAccount().getAccountNumber()).isEqualTo("10000003");
        assertThat(savedTransfer.getAmount()).isEqualByComparingTo("75.25");
        assertThat(savedTransfer.getOccurredAt()).isNotNull();
        assertThat(savedRequest.getRequestedAmount()).isEqualByComparingTo("1000.00");
        assertThat(savedRequest.getStatus()).isEqualTo(CreditRequestStatus.PENDING);
        assertThat(savedRequest.getRequestedAt()).isNotNull();
    }
}
