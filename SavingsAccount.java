package com.bank.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable domain entity representing a savings account.
 *
 * Design decisions:
 *  - Composition over inheritance: replaces BaseEntity -> FinancialObject
 *    -> AbstractBankRecord hierarchy with three cohesive component classes.
 *  - Immutability: satisfies all 5 Java immutability rules.
 *  - Object creation: Builder Pattern + Static Factory Methods.
 *  - No database or infrastructure logic; pure domain object.
 *
 * Immutability rules satisfied:
 *   1. final class — cannot be subclassed
 *   2. all fields private final
 *   3. no setters / mutators
 *   4. defensive copy on transactions (mutable type) in constructor and getter
 *   5. 'this' reference does not escape during construction
 */
public final class SavingsAccount {

    // ── Composed components (replaces deep inheritance) ───────────────────────
    private final AuditMetadata    auditMetadata;
    private final FinancialDetails  financialDetails;
    private final BankRecordDetails bankRecordDetails;

    // ── Own fields ────────────────────────────────────────────────────────────
    private final List<String> transactions; // always unmodifiable
    private final String       promoCode;
    private final double       interestRate;

    // ── Private canonical constructor — only Builder may call this ────────────
    private SavingsAccount(Builder b) {
        this.auditMetadata     = Objects.requireNonNull(b.auditMetadata,     "auditMetadata");
        this.financialDetails   = Objects.requireNonNull(b.financialDetails,   "financialDetails");
        this.bankRecordDetails  = Objects.requireNonNull(b.bankRecordDetails,  "bankRecordDetails");
        // Defensive copy: isolate internal state from the caller's list
        this.transactions      = Collections.unmodifiableList(
                                     new ArrayList<>(Objects.requireNonNull(b.transactions, "transactions")));
        this.promoCode         = b.promoCode;   // String is inherently immutable
        this.interestRate      = b.interestRate;
    }

    // ── Static Factory: standard new account ─────────────────────────────────
    /**
     * Opens a new savings account with an empty transaction history.
     * Applies safe defaults so callers don't pass null for optional fields.
     */
    public static SavingsAccount createNew(
            AuditMetadata     auditMetadata,
            FinancialDetails  financialDetails,
            BankRecordDetails bankRecordDetails,
            double            interestRate) {

        return new Builder()
                .auditMetadata(auditMetadata)
                .financialDetails(financialDetails)
                .bankRecordDetails(bankRecordDetails)
                .transactions(Collections.emptyList())
                .interestRate(interestRate)
                .build();
    }

    // ── Static Copy Factory ───────────────────────────────────────────────────
    /**
     * Produces a structurally independent copy of {@code source}.
     * All component objects are immutable so they can be shared safely;
     * the transaction list is deep-copied to guarantee independence.
     */
    public static SavingsAccount copyOf(SavingsAccount source) {
        Objects.requireNonNull(source, "source must not be null");
        return new Builder()
                .auditMetadata(source.auditMetadata)
                .financialDetails(source.financialDetails)
                .bankRecordDetails(source.bankRecordDetails)
                .transactions(new ArrayList<>(source.transactions)) // deep copy
                .promoCode(source.promoCode)
                .interestRate(source.interestRate)
                .build();
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public AuditMetadata     getAuditMetadata()     { return auditMetadata; }
    public FinancialDetails  getFinancialDetails()  { return financialDetails; }
    public BankRecordDetails getBankRecordDetails() { return bankRecordDetails; }

    /** Returns an unmodifiable view — caller cannot mutate internal state. */
    public List<String>      getTransactions()      { return transactions; }
    public String            getPromoCode()         { return promoCode; }
    public double            getInterestRate()      { return interestRate; }

    // ── Object contracts ──────────────────────────────────────────────────────

    /**
     * Two SavingsAccount instances are equal when all fields are equal.
     * Satisfies: reflexivity, symmetry, transitivity, consistency, non-nullity.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SavingsAccount)) return false;
        SavingsAccount other = (SavingsAccount) o;
        return Double.compare(other.interestRate, interestRate) == 0
            && Objects.equals(auditMetadata,     other.auditMetadata)
            && Objects.equals(financialDetails,   other.financialDetails)
            && Objects.equals(bankRecordDetails,  other.bankRecordDetails)
            && Objects.equals(transactions,       other.transactions)
            && Objects.equals(promoCode,          other.promoCode);
    }

    /** hashCode contract: equal objects must have the same hash. */
    @Override
    public int hashCode() {
        return Objects.hash(
            auditMetadata,
            financialDetails,
            bankRecordDetails,
            transactions,
            promoCode,
            interestRate);
    }

    /** Masks SSN to last 4 digits to prevent PII leakage in logs. */
    @Override
    public String toString() {
        return "SavingsAccount{"
            + "id="             + auditMetadata.getId()
            + ", createdAt='"   + auditMetadata.getCreatedAt()       + '\''
            + ", currency='"    + financialDetails.getCurrencyType() + '\''
            + ", taxable="      + financialDetails.isTaxable()
            + ", balance="      + bankRecordDetails.getLedgerBalance()
            + ", ownerSSN='****" + maskedSsn()                       + '\''
            + ", routing='"     + bankRecordDetails.getRoutingNumber() + '\''
            + ", interestRate=" + interestRate
            + ", promoCode='"   + promoCode                          + '\''
            + ", txCount="      + transactions.size()
            + '}';
    }

    private String maskedSsn() {
        String ssn = bankRecordDetails.getOwnerSSN();
        return (ssn != null && ssn.length() >= 4)
            ? ssn.substring(ssn.length() - 4)
            : "????";
    }

    // ── Builder ───────────────────────────────────────────────────────────────
    public static final class Builder {

        private AuditMetadata    auditMetadata;
        private FinancialDetails  financialDetails;
        private BankRecordDetails bankRecordDetails;
        private List<String>      transactions = new ArrayList<>();
        private String            promoCode;
        private double            interestRate;

        public Builder auditMetadata(AuditMetadata v)       { this.auditMetadata    = v; return this; }
        public Builder financialDetails(FinancialDetails v)  { this.financialDetails  = v; return this; }
        public Builder bankRecordDetails(BankRecordDetails v){ this.bankRecordDetails = v; return this; }
        public Builder transactions(List<String> v)          { this.transactions      = v; return this; }
        public Builder promoCode(String v)                   { this.promoCode         = v; return this; }
        public Builder interestRate(double v)                { this.interestRate      = v; return this; }

        public SavingsAccount build() { return new SavingsAccount(this); }
    }
}