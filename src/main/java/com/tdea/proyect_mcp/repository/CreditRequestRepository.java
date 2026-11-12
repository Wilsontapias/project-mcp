package com.tdea.proyect_mcp.repository;

import com.tdea.proyect_mcp.domain.CreditRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Persistence operations for customer credit applications. */
public interface CreditRequestRepository extends JpaRepository<CreditRequest, Long> {

    /**
     * Finds an account's credit applications, newest first.
     *
     * @param accountNumber account number
     * @return matching requests ordered newest first
     */
    List<CreditRequest> findByAccountAccountNumberOrderByRequestedAtDesc(String accountNumber);
}
