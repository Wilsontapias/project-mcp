package com.tdea.proyect_mcp.service;

import com.tdea.proyect_mcp.domain.Account;
import com.tdea.proyect_mcp.domain.AccountStatus;
import com.tdea.proyect_mcp.domain.BankTransaction;
import com.tdea.proyect_mcp.domain.CreditRequest;
import com.tdea.proyect_mcp.domain.TransactionType;
import com.tdea.proyect_mcp.repository.AccountRepository;
import com.tdea.proyect_mcp.repository.BankTransactionRepository;
import com.tdea.proyect_mcp.repository.CreditRequestRepository;
import com.tdea.proyect_mcp.service.dto.AccountSummary;
import com.tdea.proyect_mcp.service.dto.CreditRequestSummary;
import com.tdea.proyect_mcp.service.dto.TransactionSummary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements validated and transactional banking operations for the local demonstration.
 *
 * <p>This service operates only on the project's H2 database; it is not connected to a real bank.</p>
 */
@Service
public class BankingService {

    private static final int MAX_ACCOUNT_NUMBER_LENGTH = 20;
    private static final int MAX_HOLDER_NAME_LENGTH = 120;
    private static final int MAX_DESCRIPTION_LENGTH = 255;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999999999999.99");

    private final AccountRepository accountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final CreditRequestRepository creditRequestRepository;

    /**
     * Creates the service with its persistence dependencies.
     *
     * @param accountRepository account persistence
     * @param bankTransactionRepository transaction-ledger persistence
     * @param creditRequestRepository credit-request persistence
     */
    public BankingService(
            AccountRepository accountRepository,
            BankTransactionRepository bankTransactionRepository,
            CreditRequestRepository creditRequestRepository) {
        this.accountRepository = accountRepository;
        this.bankTransactionRepository = bankTransactionRepository;
        this.creditRequestRepository = creditRequestRepository;
    }

    /**
     * Creates an active account with a zero balance.
     *
     * @param accountNumber unique account number
     * @param holderName account holder's name
     * @return created account summary
     */
    @Transactional
    public AccountSummary createAccount(String accountNumber, String holderName) {
        String normalizedNumber = requireText(accountNumber, "El número de cuenta es obligatorio.",
                MAX_ACCOUNT_NUMBER_LENGTH);
        String normalizedHolder = requireText(holderName, "El nombre del titular es obligatorio.",
                MAX_HOLDER_NAME_LENGTH);

        if (accountRepository.findByAccountNumber(normalizedNumber).isPresent()) {
            throw new BankingOperationException("Ya existe una cuenta con ese número.");
        }

        try {
            Account account = accountRepository.save(
                    new Account(normalizedNumber, normalizedHolder, BigDecimal.ZERO.setScale(2)));
            return toSummary(account);
        } catch (DataIntegrityViolationException exception) {
            throw new BankingOperationException("No fue posible crear la cuenta; verifica que el número sea único.");
        }
    }

    /**
     * Returns the current balance and status of an account.
     *
     * @param accountNumber account number to query
     * @return current account summary
     */
    @Transactional(readOnly = true)
    public AccountSummary getBalance(String accountNumber) {
        return toSummary(findAccount(accountNumber));
    }

    /**
     * Deposits an amount into an active account and records the ledger entry atomically.
     *
     * @param accountNumber destination account number
     * @param amount positive amount with at most two decimal places
     * @param description reason for the deposit
     * @return saved transaction summary
     */
    @Transactional
    public TransactionSummary deposit(String accountNumber, BigDecimal amount, String description) {
        BigDecimal normalizedAmount = requireAmount(amount);
        Account account = lockAccount(accountNumber);
        requireActive(account);

        account.setBalance(account.getBalance().add(normalizedAmount));
        BankTransaction transaction = new BankTransaction(
                TransactionType.DEPOSIT,
                null,
                account,
                normalizedAmount,
                normalizeDescription(description));
        return toSummary(bankTransactionRepository.save(transaction));
    }

    /**
     * Transfers money atomically between two active accounts and writes one ledger entry.
     * Account rows are locked in number order to reduce the chance of deadlocks.
     *
     * @param sourceAccountNumber account to debit
     * @param destinationAccountNumber account to credit
     * @param amount positive amount with at most two decimal places
     * @param description reason for the transfer
     * @return saved transfer summary
     */
    @Transactional
    public TransactionSummary transfer(
            String sourceAccountNumber,
            String destinationAccountNumber,
            BigDecimal amount,
            String description) {
        String sourceNumber = requireAccountNumber(sourceAccountNumber);
        String destinationNumber = requireAccountNumber(destinationAccountNumber);
        if (sourceNumber.equals(destinationNumber)) {
            throw new BankingOperationException("La cuenta de origen y destino deben ser diferentes.");
        }

        BigDecimal normalizedAmount = requireAmount(amount);
        Map<String, Account> lockedAccounts = lockAccounts(sourceNumber, destinationNumber);
        Account source = lockedAccounts.get(sourceNumber);
        Account destination = lockedAccounts.get(destinationNumber);
        requireActive(source);
        requireActive(destination);

        if (source.getBalance().compareTo(normalizedAmount) < 0) {
            throw new BankingOperationException("Saldo insuficiente para realizar la transferencia.");
        }

        source.setBalance(source.getBalance().subtract(normalizedAmount));
        destination.setBalance(destination.getBalance().add(normalizedAmount));
        BankTransaction transaction = new BankTransaction(
                TransactionType.TRANSFER,
                source,
                destination,
                normalizedAmount,
                normalizeDescription(description));
        return toSummary(bankTransactionRepository.save(transaction));
    }

    /**
     * Stores a credit application for review. It does not approve or disburse the credit.
     *
     * @param accountNumber applicant's account number
     * @param requestedAmount positive amount with at most two decimal places
     * @param purpose reason for requesting credit
     * @return newly created pending request
     */
    @Transactional
    public CreditRequestSummary requestCredit(String accountNumber, BigDecimal requestedAmount, String purpose) {
        Account account = lockAccount(accountNumber);
        requireActive(account);
        BigDecimal normalizedAmount = requireAmount(requestedAmount);
        String normalizedPurpose = requireText(purpose, "El propósito de la solicitud es obligatorio.",
                MAX_DESCRIPTION_LENGTH);

        CreditRequest request = creditRequestRepository.save(
                new CreditRequest(account, normalizedAmount, normalizedPurpose));
        return toSummary(request);
    }

    /**
     * Returns ledger entries where the account is either the source or destination.
     *
     * @param accountNumber account number to query
     * @return transactions ordered newest first
     */
    @Transactional(readOnly = true)
    public List<TransactionSummary> getTransactionHistory(String accountNumber) {
        String normalizedNumber = requireAccountNumber(accountNumber);
        findAccount(normalizedNumber);
        return bankTransactionRepository.findAllForAccount(normalizedNumber).stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * Returns credit requests associated with an account, newest first.
     *
     * @param accountNumber account number to query
     * @return matching credit requests
     */
    @Transactional(readOnly = true)
    public List<CreditRequestSummary> getCreditRequests(String accountNumber) {
        String normalizedNumber = requireAccountNumber(accountNumber);
        findAccount(normalizedNumber);
        return creditRequestRepository.findByAccountAccountNumberOrderByRequestedAtDesc(normalizedNumber)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private Account findAccount(String accountNumber) {
        String normalizedNumber = requireAccountNumber(accountNumber);
        return accountRepository.findByAccountNumber(normalizedNumber)
                .orElseThrow(() -> new BankingOperationException("No existe la cuenta " + normalizedNumber + "."));
    }

    private Account lockAccount(String accountNumber) {
        String normalizedNumber = requireAccountNumber(accountNumber);
        return accountRepository.findByAccountNumberForUpdate(normalizedNumber)
                .orElseThrow(() -> new BankingOperationException("No existe la cuenta " + normalizedNumber + "."));
    }

    private Map<String, Account> lockAccounts(String firstNumber, String secondNumber) {
        List<String> numbers = new ArrayList<>(List.of(firstNumber, secondNumber));
        numbers.sort(Comparator.naturalOrder());
        Map<String, Account> accounts = new HashMap<>();
        for (String number : numbers) {
            Account account = accountRepository.findByAccountNumberForUpdate(number)
                    .orElseThrow(() -> new BankingOperationException("No existe la cuenta " + number + "."));
            accounts.put(number, account);
        }
        return accounts;
    }

    private String requireAccountNumber(String accountNumber) {
        return requireText(accountNumber, "El número de cuenta es obligatorio.", MAX_ACCOUNT_NUMBER_LENGTH);
    }

    private String requireText(String value, String message, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BankingOperationException(message);
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new BankingOperationException("El texto supera el máximo permitido de " + maxLength + " caracteres.");
        }
        return normalized;
    }

    private BigDecimal requireAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BankingOperationException("El monto es obligatorio.");
        }
        final BigDecimal normalized;
        try {
            normalized = amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new BankingOperationException("El monto no puede tener más de dos decimales.");
        }
        if (normalized.signum() <= 0 || normalized.compareTo(MAX_AMOUNT) > 0) {
            throw new BankingOperationException("El monto debe estar entre 0.01 y 99999999999999999.99.");
        }
        return normalized;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return requireText(description, "La descripción no es válida.", MAX_DESCRIPTION_LENGTH);
    }

    private void requireActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BankingOperationException("La cuenta " + account.getAccountNumber() + " no está activa.");
        }
    }

    private AccountSummary toSummary(Account account) {
        return new AccountSummary(account.getAccountNumber(), account.getHolderName(), account.getBalance(),
                account.getStatus(), account.getCreatedAt().toString());
    }

    private TransactionSummary toSummary(BankTransaction transaction) {
        return new TransactionSummary(
                transaction.getReference(),
                transaction.getType(),
                transaction.getSourceAccount() == null ? null : transaction.getSourceAccount().getAccountNumber(),
                transaction.getDestinationAccount().getAccountNumber(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getOccurredAt().toString());
    }

    private CreditRequestSummary toSummary(CreditRequest request) {
        return new CreditRequestSummary(
                request.getId(),
                request.getAccount().getAccountNumber(),
                request.getRequestedAmount(),
                request.getPurpose(),
                request.getStatus(),
                request.getRequestedAt().toString());
    }
}
