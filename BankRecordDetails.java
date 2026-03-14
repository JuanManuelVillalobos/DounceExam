package com.bank.domain;

import java.util.Objects;

public final class BankRecordDetails {
    private final double ledgerBalance;
    private final String ownerSSN;
    private final String routingNumber;

    public BankRecordDetails(double ledgerBalance, String ownerSSN, String routingNumber) {
        this.ledgerBalance = ledgerBalance;
        this.ownerSSN      = Objects.requireNonNull(ownerSSN);
        this.routingNumber = Objects.requireNonNull(routingNumber);
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

    @Override public int hashCode() { return Objects.hash(ledgerBalance, ownerSSN, routingNumber); }

    @Override
    public String toString() {
        return "BankRecordDetails{balance=" + ledgerBalance
            + ", ssn='[PROTECTED]'"
            + ", routing='" + routingNumber + "'}";
    }
}