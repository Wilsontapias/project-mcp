package com.tdea.proyect_mcp.tools;

import com.tdea.proyect_mcp.service.BankingService;
import com.tdea.proyect_mcp.service.dto.AccountSummary;
import com.tdea.proyect_mcp.service.dto.CreditRequestSummary;
import com.tdea.proyect_mcp.service.dto.TransactionSummary;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * MCP tool facade for the local banking demonstration.
 *
 * <p>All operations affect only the project's temporary H2 database and are not real bank operations.</p>
 */
@Service
public class BankingMcpTools {

    private final BankingService bankingService;

    /**
     * Creates the tool facade.
     *
     * @param bankingService validated banking application service
     */
    public BankingMcpTools(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    /**
     * Creates an active account with an initial balance of zero.
     *
     * @param accountNumber unique account number
     * @param holderName account holder's name
     * @return created account summary
     */
    @Tool(name = "createAccount", description = "Crea una cuenta bancaria local de demostración con saldo cero. "
            + "Úsala solo si el usuario solicita explícitamente crear una cuenta. No crea una cuenta bancaria real.")
    public AccountSummary createAccount(
            @ToolParam(description = "Número único de cuenta, hasta 20 caracteres") String accountNumber,
            @ToolParam(description = "Nombre completo del titular de la cuenta") String holderName) {
        return bankingService.createAccount(accountNumber, holderName);
    }

    /**
     * Looks up an account's balance and status.
     *
     * @param accountNumber account number to query
     * @return account summary
     */
    @Tool(name = "getBalance", description = "Consulta el saldo y el estado actual de una cuenta de demostración local.")
    public AccountSummary getBalance(
            @ToolParam(description = "Número de cuenta que se va a consultar") String accountNumber) {
        return bankingService.getBalance(accountNumber);
    }

    /**
     * Deposits funds into an account in the local H2 database.
     *
     * @param accountNumber destination account number
     * @param amount positive amount in the demonstration currency, with at most two decimal places
     * @param description reason for the deposit
     * @return persisted deposit transaction
     */
    @Tool(name = "deposit", description = "Consigna un monto positivo en una cuenta de demostración local activa. "
            + "Solo cambia los datos H2 del proyecto, no una cuenta bancaria real. Úsala solo tras una solicitud "
            + "explícita del usuario.")
    public TransactionSummary deposit(
            @ToolParam(description = "Número de la cuenta destino") String accountNumber,
            @ToolParam(description = "Monto positivo a consignar, con máximo dos decimales") BigDecimal amount,
            @ToolParam(description = "Motivo breve de la consignación") String description) {
        return bankingService.deposit(accountNumber, amount, description);
    }

    /**
     * Transfers funds between active local demonstration accounts.
     *
     * @param sourceAccountNumber account to debit
     * @param destinationAccountNumber account to credit
     * @param amount positive amount in the demonstration currency, with at most two decimal places
     * @param description reason for the transfer
     * @return persisted transfer transaction
     */
    @Tool(name = "transfer", description = "Transfiere un monto positivo entre dos cuentas de demostración locales "
            + "activas. La operación es atómica y se rechaza si el origen no tiene saldo suficiente. Solo afecta la "
            + "base H2 del proyecto, no un banco real. Úsala solo tras una solicitud explícita del usuario.")
    public TransactionSummary transfer(
            @ToolParam(description = "Número de cuenta de origen que se debitará") String sourceAccountNumber,
            @ToolParam(description = "Número de cuenta destino que recibirá el dinero") String destinationAccountNumber,
            @ToolParam(description = "Monto positivo a transferir, con máximo dos decimales") BigDecimal amount,
            @ToolParam(description = "Motivo breve de la transferencia") String description) {
        return bankingService.transfer(sourceAccountNumber, destinationAccountNumber, amount, description);
    }

    /**
     * Records a credit application in pending status; it does not approve or disburse funds.
     *
     * @param accountNumber applicant's account number
     * @param requestedAmount positive amount requested
     * @param purpose reason for the request
     * @return newly persisted request
     */
    @Tool(name = "requestCredit", description = "Registra una solicitud de crédito pendiente para una cuenta de "
            + "demostración local activa. Solo registra la solicitud; no aprueba ni desembolsa dinero y no es una "
            + "solicitud bancaria real. Úsala solo tras una solicitud explícita del usuario.")
    public CreditRequestSummary requestCredit(
            @ToolParam(description = "Número de cuenta del solicitante") String accountNumber,
            @ToolParam(description = "Monto positivo solicitado, con máximo dos decimales") BigDecimal requestedAmount,
            @ToolParam(description = "Motivo de la solicitud de crédito") String purpose) {
        return bankingService.requestCredit(accountNumber, requestedAmount, purpose);
    }

    /**
     * Lists movements where the account was the source or destination.
     *
     * @param accountNumber account number to query
     * @return transaction history, newest first
     */
    @Tool(name = "getTransactionHistory", description = "Consulta consignaciones y transferencias de una cuenta de "
            + "demostración local, de la más reciente a la más antigua. Solo lee la base H2 del proyecto.")
    public List<TransactionSummary> getTransactionHistory(
            @ToolParam(description = "Número de cuenta cuyo historial se solicita") String accountNumber) {
        return bankingService.getTransactionHistory(accountNumber);
    }

    /**
     * Lists credit applications for an account.
     *
     * @param accountNumber account number to query
     * @return credit requests, newest first
     */
    @Tool(name = "getCreditRequests", description = "Consulta las solicitudes de crédito de una cuenta de "
            + "demostración local, de la más reciente a la más antigua. Las solicitudes permanecen pendientes "
            + "hasta que se revisen por separado.")
    public List<CreditRequestSummary> getCreditRequests(
            @ToolParam(description = "Número de cuenta cuyas solicitudes de crédito se consultan") String accountNumber) {
        return bankingService.getCreditRequests(accountNumber);
    }
}
