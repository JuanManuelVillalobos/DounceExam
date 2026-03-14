package com.bank.domain;

import java.util.Objects;

/**
 * Immutable value object holding core banking record data.
 * Replaces the fields previously inherited from AbstractBankRecord.
 *
 * Immutability rules satisfied:
 *   1. final class
 *   2. all fields private final
 *   3. no setters / mutators
 *   4. no mutable reference types
 *   5. constructor validates inputs eagerly
 */
public final class BankRecordDetails {

    private final double ledgerBalance;
    private final String ownerSSN;
    private final String routingNumber;

    public BankRecordDetails(double ledgerBalance, String ownerSSN, String routingNumber) {
        if (ledgerBalance < 0) throw new IllegalArgumentException("ledgerBalance cannot be negative");
        this.ledgerBalance = ledgerBalance;
        this.ownerSSN      = Objects.requireNonNull(ownerSSN,      "ownerSSN must not be null");
        this.routingNumber = Objects.requireNonNull(routingNumber, "routingNumber must not be null");
    }

    public double getLedgerBalance() { return ledgerBalance; }
    public String getOwnerSSN()      { return ownerSSN; }
    public String getRoutingNumber() { return routingNumber; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankRecordDetails)) return false;
        BankRecordDetails other = (BankRecordDetails) o;
        return Double.compare(other.ledgerBalance, ledgerBalance) == 0
            && Objects.equals(ownerSSN,      other.ownerSSN)
            && Objects.equals(routingNumber, other.routingNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ledgerBalance, ownerSSN, routingNumber);
    }

    /** SSN is intentionally hidden to prevent PII leakage in logs. */
    @Override
    public String toString() {
        return "BankRecordDetails{balance=" + ledgerBalance
            + ", ssn='[PROTECTED]'"
            + ", routing='" + routingNumber + "'}";
    }
}