package com.bank.payment;

import com.bank.domain.AuditMetadata;
import com.bank.domain.BankRecordDetails;
import com.bank.domain.SavingsAccount;
import com.bank.repository.SavingsAccountRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for applying payments to savings accounts.
 *
 * Design decisions:
 *  - Single Responsibility: only handles payment processing logic.
 *  - Because SavingsAccount is immutable, "applying a payment" means
 *    building a NEW SavingsAccount instance with the updated balance and
 *    appended transaction, then persisting it via the repository.
 *  - Constructor injection: SavingsAccountRepository is injected,
 *    keeping this service decoupled from any specific DB technology.
 *
 * In the legacy design, MonolithFintechManager.processPayment() reached
 * directly into a hardcoded MySQLConnection. Here, persistence is
 * delegated to the repository abstraction.
 */
public final class PaymentService {

    private final SavingsAccountRepository repository;

    public PaymentService(SavingsAccountRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    /**
     * Applies a deposit to the given account.
     * Returns the new (immutable) account state — the original is unchanged.
     *
     * @param account the current account state
     * @param amount  the deposit amount; must be positive
     * @return a new SavingsAccount reflecting the updated balance
     * @throws IllegalArgumentException if amount is not positive
     */
    public SavingsAccount deposit(SavingsAccount account, double amount) {
        Objects.requireNonNull(account, "account must not be null");
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive");

        double newBalance     = account.getBankRecordDetails().getLedgerBalance() + amount;
        SavingsAccount updated = buildUpdatedAccount(account, newBalance, "DEPOSIT:" + amount);

        repository.update(updated);
        return updated;
    }

    /**
     * Applies a withdrawal to the given account.
     * Returns the new (immutable) account state — the original is unchanged.
     *
     * @param account the current account state
     * @param amount  the withdrawal amount; must be positive and <= current balance
     * @return a new SavingsAccount reflecting the updated balance
     * @throws IllegalArgumentException  if amount is not positive
     * @throws InsufficientFundsException if balance would go negative
     */
    public SavingsAccount withdraw(SavingsAccount account, double amount) {
        Objects.requireNonNull(account, "account must not be null");
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive");

        double currentBalance = account.getBankRecordDetails().getLedgerBalance();
        if (amount > currentBalance) {
            throw new InsufficientFundsException(
                "Insufficient funds: balance=" + currentBalance + ", requested=" + amount);
        }

        double newBalance     = currentBalance - amount;
        SavingsAccount updated = buildUpdatedAccount(account, newBalance, "WITHDRAWAL:" + amount);

        repository.update(updated);
        return updated;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Builds a new immutable SavingsAccount with the updated balance
     * and the new transaction appended — without mutating the original.
     */
    private SavingsAccount buildUpdatedAccount(SavingsAccount original,
                                               double newBalance,
                                               String transactionEntry) {
        // Build new BankRecordDetails with updated balance
        BankRecordDetails updatedBankDetails = new BankRecordDetails(
            newBalance,
            original.getBankRecordDetails().getOwnerSSN(),
            original.getBankRecordDetails().getRoutingNumber()
        );

        // Build updated transaction list (defensive: start from unmodifiable original)
        List<String> updatedTransactions = new ArrayList<>(original.getTransactions());
        updatedTransactions.add(transactionEntry);

        return new SavingsAccount.Builder()
                .auditMetadata(original.getAuditMetadata())
                .financialDetails(original.getFinancialDetails())
                .bankRecordDetails(updatedBankDetails)
                .transactions(updatedTransactions)
                .promoCode(original.getPromoCode())
                .interestRate(original.getInterestRate())
                .build();
    }

    // ── Domain exception ──────────────────────────────────────────────────────
    public static final class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }
}