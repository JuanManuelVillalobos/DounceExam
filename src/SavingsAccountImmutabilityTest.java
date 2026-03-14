package com.bank.domain.src;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SavingsAccount – Immutability Stress Tests")
class SavingsAccountImmutabilityTest {

    private SavingsAccount account;

    // ── Shared fixture ────────────────────────────────────────────────────────
    @BeforeEach
    void setUp() {
        AuditMetadata audit = new AuditMetadata(1, "2025-01-01", "2025-06-01");
        FinancialDetails fin = new FinancialDetails("AUD-001", true, "USD");
        BankRecordDetails bank = new BankRecordDetails(5000.00, "123-45-6789", "021000021");

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

    // ── Test 1: Constructor reference leakage ─────────────────────────────────
    /**
     * Verifies that mutating the external list used during construction
     * does NOT affect the internal state of SavingsAccount.
     * This proves the constructor performs a defensive copy.
     */
    @Test
    @DisplayName("Constructor defensive copy: external list mutation must not affect account")
    void testNoExternalMutationViaConstructorReferenceLeakage() {
        // Arrange – build a fresh account using a controllable external list
        ArrayList<String> externalList = new ArrayList<>();
        externalList.add("TX-AAA");

        AuditMetadata audit  = new AuditMetadata(99, "2025-01-01", "2025-01-01");
        FinancialDetails fin  = new FinancialDetails("AUD-X", false, "MXN");
        BankRecordDetails bank = new BankRecordDetails(1000.0, "999-99-9999", "021000021");

        SavingsAccount freshAccount = new SavingsAccount.Builder()
                .auditMetadata(audit)
                .financialDetails(fin)
                .bankRecordDetails(bank)
                .transactions(externalList)   // pass our external list
                .interestRate(2.0)
                .build();

        int sizeBeforeMutation = freshAccount.getTransactions().size(); // should be 1

        // Act – mutate the external list AFTER construction
        externalList.add("TX-INJECTED");
        externalList.add("TX-INJECTED-2");

        // Assert – internal state must remain unchanged
        assertEquals(sizeBeforeMutation, freshAccount.getTransactions().size(),
            "Internal transaction list size must NOT change when external list is mutated.");

        assertFalse(freshAccount.getTransactions().contains("TX-INJECTED"),
            "Injected element must NOT appear inside the account's transaction list.");
    }

    // ── Test 2: Getter reference leakage ─────────────────────────────────────
    /**
     * Verifies that the list returned by getTransactions() is unmodifiable.
     * Any attempt to add/remove elements must throw UnsupportedOperationException,
     * guaranteeing that callers cannot mutate internal state via the getter.
     */
    @Test
    @DisplayName("Getter returns unmodifiable list: mutation attempt must throw UnsupportedOperationException")
    void testNoExternalMutationViaGetterReferenceLeakage() {
        // Arrange
        List<String> leakedReference = account.getTransactions();

        // Act + Assert
        assertThrows(UnsupportedOperationException.class,
            () -> leakedReference.add("TX-MALICIOUS"),
            "Adding to the returned transaction list must throw UnsupportedOperationException.");

        assertThrows(UnsupportedOperationException.class,
            () -> leakedReference.remove(0),
            "Removing from the returned transaction list must throw UnsupportedOperationException.");
    }

    // ── Bonus: copyOf independence ────────────────────────────────────────────
    @Test
    @DisplayName("copyOf produces an independent copy; mutating original builder does not affect copy")
    void testCopyOfIsIndependent() {
        SavingsAccount copy = SavingsAccount.copyOf(account);

        // Structural equality
        assertEquals(account, copy, "copyOf must produce an equal account.");

        // Independence: the two lists are different objects
        assertNotSame(account.getTransactions(), copy.getTransactions(),
            "copyOf must produce a distinct transaction list instance.");
    }
}