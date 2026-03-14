package com.bank.domain.src;

import java.util.Objects;

public record BankRecordDetails(double ledgerBalance, String ownerSSN, String routingNumber) {
    public BankRecordDetails(double ledgerBalance, String ownerSSN, String routingNumber) {
        this.ledgerBalance = ledgerBalance;
        this.ownerSSN = Objects.requireNonNull(ownerSSN);
        this.routingNumber = Objects.requireNonNull(routingNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankRecordDetails(double balance, String ssn, String number))) return false;
        return Double.compare(balance, ledgerBalance) == 0
                && Objects.equals(ownerSSN, ssn)
                && Objects.equals(routingNumber, number);
    }

    @Override
    public String toString() {
        return "BankRecordDetails{balance=" + ledgerBalance
                + ", ssn='[PROTECTED]'"
                + ", routing='" + routingNumber + "'}";
    }
}