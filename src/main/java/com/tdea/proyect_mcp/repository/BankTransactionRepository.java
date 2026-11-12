package com.tdea.proyect_mcp.repository;

import com.tdea.proyect_mcp.domain.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Persistence operations for the immutable transaction ledger. */
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    /**
     * Finds a ledger entry by its externally returned reference.
     *
     * @param reference transaction reference
     * @return matching transaction, if one exists
     */
    Optional<BankTransaction> findByReference(String reference);

    /**
     * Finds all transactions where the account is either source or destination.
     *
     * @param accountNumber account whose ledger is requested
     * @return transactions ordered newest first
     */
    @Query("select transaction from BankTransaction transaction "
            + "left join transaction.sourceAccount source "
            + "left join transaction.destinationAccount destination "
            + "where source.accountNumber = :accountNumber "
            + "or destination.accountNumber = :accountNumber "
            + "order by transaction.occurredAt desc")
    List<BankTransaction> findAllForAccount(@Param("accountNumber") String accountNumber);
}
