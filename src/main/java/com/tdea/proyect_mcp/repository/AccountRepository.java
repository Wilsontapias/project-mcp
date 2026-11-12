package com.tdea.proyect_mcp.repository;

import com.tdea.proyect_mcp.domain.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Persistence operations for bank accounts. */
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Finds an account by its public account number.
     *
     * @param accountNumber unique account number
     * @return matching account, if one exists
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Obtains a write lock on an account for a balance-changing transaction.
     *
     * @param accountNumber unique account number
     * @return matching locked account, if one exists
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from Account account where account.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
}
