package com.tdea.proyect_mcp.service;

import com.tdea.proyect_mcp.domain.AccountStatus;
import com.tdea.proyect_mcp.domain.CreditRequestStatus;
import com.tdea.proyect_mcp.domain.TransactionType;
import com.tdea.proyect_mcp.service.dto.AccountSummary;
import com.tdea.proyect_mcp.service.dto.CreditRequestSummary;
import com.tdea.proyect_mcp.service.dto.TransactionSummary;
import com.tdea.proyect_mcp.tools.BankingMcpTools;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Integration tests for banking operations and MCP tool registration. */
@SpringBootTest(properties = "spring.ai.mcp.server.enabled=false")
@Transactional
class BankingServiceTests {

    @Autowired
    private BankingService bankingService;

    @Autowired
    private BankingMcpTools bankingMcpTools;

    @Autowired
    private ToolCallbackProvider bankingTools;

    @Test
    void createsAccountWithZeroBalanceAndReturnsItsSummary() {
        AccountSummary account = bankingService.createAccount("20000001", "Test Holder");

        assertThat(account.accountNumber()).isEqualTo("20000001");
        assertThat(account.balance()).isEqualByComparingTo("0.00");
        assertThat(account.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.createdAt()).contains("T");
        assertThat(bankingService.getBalance("20000001").holderName()).isEqualTo("Test Holder");
    }

    @Test
    void depositIncreasesBalanceAndCreatesLedgerEntry() {
        bankingService.createAccount("20000002", "Deposit Holder");

        TransactionSummary deposit = bankingService.deposit(
                "20000002", new BigDecimal("125.50"), "Cash deposit");

        assertThat(deposit.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(deposit.sourceAccountNumber()).isNull();
        assertThat(deposit.destinationAccountNumber()).isEqualTo("20000002");
        assertThat(deposit.reference()).isNotBlank();
        assertThat(deposit.occurredAt()).contains("T");
        assertThat(bankingService.getBalance("20000002").balance()).isEqualByComparingTo("125.50");
        assertThat(bankingService.getTransactionHistory("20000002")).hasSize(1);
    }

    @Test
    void transferMovesFundsAndAppearsInBothAccountHistories() {
        bankingService.createAccount("20000003", "Source Holder");
        bankingService.createAccount("20000004", "Destination Holder");
        bankingService.deposit("20000003", new BigDecimal("100.00"), "Initial test funds");

        TransactionSummary transfer = bankingService.transfer(
                "20000003", "20000004", new BigDecimal("35.25"), "Test transfer");

        assertThat(transfer.type()).isEqualTo(TransactionType.TRANSFER);
        assertThat(bankingService.getBalance("20000003").balance()).isEqualByComparingTo("64.75");
        assertThat(bankingService.getBalance("20000004").balance()).isEqualByComparingTo("35.25");
        assertThat(bankingService.getTransactionHistory("20000003")).hasSize(2);
        assertThat(bankingService.getTransactionHistory("20000004")).hasSize(1);
    }

    @Test
    void rejectsTransferWithoutChangingBalancesOrWritingTransferRecord() {
        bankingService.createAccount("20000005", "Poor Source");
        bankingService.createAccount("20000006", "Destination");
        bankingService.deposit("20000005", new BigDecimal("10.00"), "Initial funds");

        assertThatThrownBy(() -> bankingService.transfer(
                "20000005", "20000006", new BigDecimal("10.01"), "Too much"))
                .isInstanceOf(BankingOperationException.class)
                .hasMessageContaining("Saldo insuficiente");

        assertThat(bankingService.getBalance("20000005").balance()).isEqualByComparingTo("10.00");
        assertThat(bankingService.getBalance("20000006").balance()).isEqualByComparingTo("0.00");
        assertThat(bankingService.getTransactionHistory("20000005")).hasSize(1);
    }

    @Test
    void rejectsInvalidAmountsAndSelfTransfers() {
        bankingService.createAccount("20000007", "Validation Holder");
        bankingService.createAccount("20000008", "Other Holder");

        assertThatThrownBy(() -> bankingService.deposit(
                "20000007", new BigDecimal("0.00"), "Invalid"))
                .isInstanceOf(BankingOperationException.class);
        assertThatThrownBy(() -> bankingService.deposit(
                "20000007", new BigDecimal("1.001"), "Invalid precision"))
                .isInstanceOf(BankingOperationException.class)
                .hasMessageContaining("dos decimales");
        assertThatThrownBy(() -> bankingService.transfer(
                "20000007", "20000007", new BigDecimal("1.00"), "Self transfer"))
                .isInstanceOf(BankingOperationException.class)
                .hasMessageContaining("deben ser diferentes");
    }

    @Test
    void creditRequestIsPendingAndDoesNotChangeAccountBalance() {
        bankingService.createAccount("20000009", "Credit Applicant");

        CreditRequestSummary request = bankingService.requestCredit(
                "20000009", new BigDecimal("5000.00"), "Education");

        assertThat(request.status()).isEqualTo(CreditRequestStatus.PENDING);
        assertThat(request.requestedAmount()).isEqualByComparingTo("5000.00");
        assertThat(bankingService.getBalance("20000009").balance()).isEqualByComparingTo("0.00");
        assertThat(bankingService.getCreditRequests("20000009")).containsExactly(request);
    }

    @Test
    void rejectsDuplicateAccountNumbers() {
        bankingService.createAccount("20000010", "First Holder");

        assertThatThrownBy(() -> bankingService.createAccount("20000010", "Second Holder"))
                .isInstanceOf(BankingOperationException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void registersAllBankingMethodsAsMcpTools() {
        Set<String> toolNames = Arrays.stream(bankingTools.getToolCallbacks())
                .map(ToolCallback::getToolDefinition)
                .map(definition -> definition.name())
                .collect(Collectors.toSet());

        assertThat(toolNames).containsExactlyInAnyOrder(
                "createAccount",
                "getBalance",
                "deposit",
                "transfer",
                "requestCredit",
                "getTransactionHistory",
                "getCreditRequests");
        assertThat(bankingMcpTools).isNotNull();
    }
}
