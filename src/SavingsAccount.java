package com.bank.domain.src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SavingsAccount {

    private final AuditMetadata    auditMetadata;
    private final FinancialDetails  financialDetails;
    private final BankRecordDetails bankRecordDetails;
    private final List<String>      transactions;
    private final String            promoCode;
    private final double            interestRate;

    // ── Private canonical constructor ─────────────────────────────────────────
    private SavingsAccount(Builder b) {
        this.auditMetadata     = Objects.requireNonNull(b.auditMetadata,    "auditMetadata");
        this.financialDetails   = Objects.requireNonNull(b.financialDetails,  "financialDetails");
        this.bankRecordDetails  = Objects.requireNonNull(b.bankRecordDetails, "bankRecordDetails");
        this.transactions       = Collections.unmodifiableList(
                                      new ArrayList<>(Objects.requireNonNull(b.transactions)));
        this.promoCode          = b.promoCode;
        this.interestRate       = b.interestRate;
    }

    // ── Static Factory Method: standard new account ───────────────────────────
    /**
     * Convenience factory for opening a brand-new savings account.
     * Applies sensible defaults (empty transaction list, no promo code).
     */
    public static SavingsAccount createNew(
            AuditMetadata    auditMetadata,
            FinancialDetails financialDetails,
            BankRecordDetails bankRecordDetails,
            double           interestRate) {

        return new Builder()
                .auditMetadata(auditMetadata)
                .financialDetails(financialDetails)
                .bankRecordDetails(bankRecordDetails)
                .transactions(Collections.emptyList())
                .promoCode(null)
                .interestRate(interestRate)
                .build();
    }

    // ── Static Copy Factory ───────────────────────────────────────────────────
    /**
     * Produces a structurally independent copy of {@code source}.
     * The internal transaction list is deep-copied so mutations to the
     * copy's builder do not bleed into the original.
     */
    public static SavingsAccount copyOf(SavingsAccount source) {
        Objects.requireNonNull(source, "source account must not be null");
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
    public List<String>      getTransactions()      { return transactions; }
    public String            getPromoCode()         { return promoCode; }
    public double            getInterestRate()      { return interestRate; }

    // ── Builder ───────────────────────────────────────────────────────────────
    public static final class Builder {

        private AuditMetadata    auditMetadata;
        private FinancialDetails  financialDetails;
        private BankRecordDetails bankRecordDetails;
        private List<String>      transactions = new ArrayList<>();
        private String            promoCode;
        private double            interestRate;

        public Builder auditMetadata(AuditMetadata v)      { this.auditMetadata    = v; return this; }
        public Builder financialDetails(FinancialDetails v) { this.financialDetails  = v; return this; }
        public Builder bankRecordDetails(BankRecordDetails v){ this.bankRecordDetails = v; return this; }
        public Builder transactions(List<String> v)         { this.transactions      = v; return this; }
        public Builder promoCode(String v)                  { this.promoCode         = v; return this; }
        public Builder interestRate(double v)               { this.interestRate      = v; return this; }

        public SavingsAccount build() { return new SavingsAccount(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                          // reflexivity
        if (!(o instanceof SavingsAccount)) return false;   // type safety
        SavingsAccount other = (SavingsAccount) o;
        // Identity is determined by the account's unique audit id
        return Double.compare(other.interestRate, interestRate) == 0
            && Objects.equals(auditMetadata,    other.auditMetadata)
            && Objects.equals(financialDetails,  other.financialDetails)
            && Objects.equals(bankRecordDetails, other.bankRecordDetails)
            && Objects.equals(transactions,      other.transactions)
            && Objects.equals(promoCode,         other.promoCode);
    }

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

    /** Sensitive fields (ownerSSN) are masked to avoid leaking PII in logs. */
    @Override
    public String toString() {
        return "SavingsAccount{"
            + "id="            + auditMetadata.id()
            + ", createdAt='"  + auditMetadata.createdAt()  + '\''
            + ", currency='"   + financialDetails.currencyType() + '\''
            + ", taxable="     + financialDetails.isTaxable()
            + ", balance="     + bankRecordDetails.ledgerBalance()
            + ", ownerSSN='****" + maskedSsn() + '\''
            + ", routing='"    + bankRecordDetails.routingNumber() + '\''
            + ", interestRate=" + interestRate
            + ", promoCode='"  + promoCode + '\''
            + ", txCount="     + transactions.size()
            + '}';
    }

    private String maskedSsn() {
        String ssn = bankRecordDetails.ownerSSN();
        return ssn != null && ssn.length() >= 4
            ? ssn.substring(ssn.length() - 4)
            : "????";
    }
}