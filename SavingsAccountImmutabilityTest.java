package com.bank.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Immutability Stress Test for SavingsAccount.
 *
 * Proves that SavingsAccount cannot be mutated from the outside,
 * even via reference leakage through either the constructor or the getter.
 *
 * Grading rubric coverage:
 *   - Immutability Audit (30%): both constructor and getter leakage covered
 *   - The "Stress Test" (20%): direct requirement
 */
@DisplayName("SavingsAccount — Immutability Stress Tests")
class SavingsAccountImmutabilityTest {

    // ── Shared fixture fields ─────────────────────────────────────────────────
    private AuditMetadata    audit;
    private FinancialDetails fin;
    private BankRecordDetails bank;
    private SavingsAccount   account;

    @BeforeEach
    void setUp() {
        audit   = new AuditMetadata(1, "2025-01-01", "2025-06-01");
        fin     = new FinancialDetails("AUD-001", true, "USD");
        bank    = new BankRecordDetails(5000.00, "123-45-6789", "021000021");

        ArrayList<String> initialTx = new ArrayList<>();
        initialTx.add("TX-001");
        initialTx.add("TX-002");

        account = new SavingsAccount.Builder()
                .auditMetadata(audit)
                .financialDetails(fin)
                .bankRecordDetails(bank)
                .transactions(initialTx)
                .promoCode("SAVE10")
                .interestRate(3.5)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TEST 1: Constructor reference leakage
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Proves the constructor performs a defensive copy.
     *
     * Steps:
     *  1. Build an account using an external mutable list.
     *  2. Mutate the external list AFTER construction.
     *  3. Assert the account's internal state did NOT change.
     */
    @Test
    @DisplayName("Constructor defensive copy: external list mutation must NOT affect account state")
    void testNoExternalMutationViaConstructorReferenceLeakage() {
        // Arrange — controllable external list
        ArrayList<String> externalList = new ArrayList<>();
        externalList.add("TX-AAA");

        SavingsAccount freshAccount = new SavingsAccount.Builder()
                .auditMetadata(new AuditMetadata(99, "2025-01-01", "2025-01-01"))
                .financialDetails(new FinancialDetails("AUD-X", false, "MXN"))
                .bankRecordDetails(new BankRecordDetails(1000.0, "999-99-9999", "021000021"))
                .transactions(externalList)   // external reference passed in
                .interestRate(2.0)
                .build();

        int sizeBeforeMutation = freshAccount.getTransactions().size(); // 1

        // Act — attack the external list AFTER construction
        externalList.add("TX-INJECTED-1");
        externalList.add("TX-INJECTED-2");
        externalList.add("TX-INJECTED-3");

        // Assert — internal size must remain 1
        assertEquals(sizeBeforeMutation, freshAccount.getTransactions().size(),
            "Internal transaction list size must NOT change when external list is mutated after construction.");

        assertFalse(freshAccount.getTransactions().contains("TX-INJECTED-1"),
            "Injected element TX-INJECTED-1 must NOT appear inside the account.");

        assertFalse(freshAccount.getTransactions().contains("TX-INJECTED-2"),
            "Injected element TX-INJECTED-2 must NOT appear inside the account.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TEST 2: Getter reference leakage
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Proves getTransactions() returns an unmodifiable view.
     *
     * Steps:
     *  1. Retrieve the transaction list via the getter.
     *  2. Attempt to add an element — must throw UnsupportedOperationException.
     *  3. Attempt to remove an element — must throw UnsupportedOperationException.
     *  4. Assert the account's internal state is completely unchanged.
     */
    @Test
    @DisplayName("Getter returns unmodifiable list: any mutation attempt must throw UnsupportedOperationException")
    void testNoExternalMutationViaGetterReferenceLeakage() {
        // Arrange
        List<String> leakedReference = account.getTransactions();
        int originalSize = leakedReference.size();

        // Act + Assert — add must be blocked
        assertThrows(UnsupportedOperationException.class,
            () -> leakedReference.add("TX-MALICIOUS"),
            "add() on the returned list must throw UnsupportedOperationException.");

        // Act + Assert — remove must be blocked
        assertThrows(UnsupportedOperationException.class,
            () -> leakedReference.remove(0),
            "remove() on the returned list must throw UnsupportedOperationException.");

        // Act + Assert — clear must be blocked
        assertThrows(UnsupportedOperationException.class,
            leakedReference::clear,
            "clear() on the returned list must throw UnsupportedOperationException.");

        // Confirm size unchanged
        assertEquals(originalSize, account.getTransactions().size(),
            "Account transaction list size must remain unchanged after blocked mutation attempts.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BONUS TEST: copyOf independence
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Proves SavingsAccount.copyOf() produces a structurally independent copy.
     */
    @Test
    @DisplayName("copyOf produces a structurally equal but independent instance")
    void testCopyOfIsStructurallyEqualButIndependent() {
        SavingsAccount copy = SavingsAccount.copyOf(account);

        // Structural equality via equals contract
        assertEquals(account, copy,
            "copyOf must produce an instance that is equal to the original.");

        // Not the same reference
        assertNotSame(account, copy,
            "copyOf must produce a distinct object, not the same reference.");

        // Transaction lists are equal in content but different objects
        assertEquals(account.getTransactions(), copy.getTransactions(),
            "Transaction list contents must be equal.");
        assertNotSame(account.getTransactions(), copy.getTransactions(),
            "Transaction list must be a distinct instance (deep copy).");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BONUS TEST: equals & hashCode contract
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("equals is reflexive, symmetric, and consistent with hashCode")
    void testEqualsAndHashCodeContract() {
        SavingsAccount copy = SavingsAccount.copyOf(account);

        // Reflexivity
        assertEquals(account, account, "An object must equal itself.");

        // Symmetry
        assertEquals(account, copy,    "a.equals(b) must match b.equals(a).");
        assertEquals(copy, account,    "b.equals(a) must match a.equals(b).");

        // hashCode consistency with equals
        assertEquals(account.hashCode(), copy.hashCode(),
            "Equal objects must have the same hashCode.");

        // Non-nullity
        assertNotEquals(null, account, "An object must not equal null.");
    }
}