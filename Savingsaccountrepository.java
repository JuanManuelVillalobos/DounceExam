package com.bank.repository;

import com.bank.domain.SavingsAccount;

import java.util.Optional;

/**
 * Repository interface for SavingsAccount persistence.
 *
 * Design decisions:
 *  - Interface-based: the domain layer depends on this abstraction,
 *    not on a concrete MySQLConnection. This is the Dependency Inversion Principle.
 *  - Returns Optional<SavingsAccount> on find operations to force callers
 *    to handle the "not found" case explicitly (no null returns).
 *  - The domain object SavingsAccount is pure; all DB concerns live here.
 *
 * A concrete implementation (e.g., MySQLSavingsAccountRepository) wires
 * a real JDBC DataSource and maps rows to/from SavingsAccount.
 */
public interface SavingsAccountRepository {

    /**
     * Persists a new account. Throws if an account with the same id already exists.
     *
     * @param account the account to save; must not be null
     */
    void save(SavingsAccount account);

    /**
     * Finds an account by its unique id.
     *
     * @param id the account id to look up
     * @return an Optional containing the account, or empty if not found
     */
    Optional<SavingsAccount> findById(int id);

    /**
     * Finds an account by the owner's SSN.
     *
     * @param ownerSSN the SSN to search for; must not be null
     * @return an Optional containing the account, or empty if not found
     */
    Optional<SavingsAccount> findByOwnerSSN(String ownerSSN);

    /**
     * Replaces the persisted state of an existing account with the new state.
     * Because SavingsAccount is immutable, "updating" means persisting a
     * new version of the object that carries the changed state.
     *
     * @param account the new account state; must not be null
     */
    void update(SavingsAccount account);

    /**
     * Removes an account from the store.
     *
     * @param id the id of the account to delete
     */
    void deleteById(int id);
}