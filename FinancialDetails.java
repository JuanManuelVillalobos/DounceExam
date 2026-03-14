package com.bank.domain;

import java.util.Objects;

/**
 * Immutable value object holding financial classification data.
 * Replaces the fields previously inherited from FinancialObject.
 *
 * Immutability rules satisfied:
 *   1. final class
 *   2. all fields private final
 *   3. no setters / mutators
 *   4. no mutable reference types
 *   5. constructor validates inputs eagerly
 */
public final class FinancialDetails {

    private final String  internalAuditCode;
    private final boolean isTaxable;
    private final String  currencyType;

    public FinancialDetails(String internalAuditCode, boolean isTaxable, String currencyType) {
        this.internalAuditCode = Objects.requireNonNull(internalAuditCode, "internalAuditCode must not be null");
        this.isTaxable         = isTaxable;
        this.currencyType      = Objects.requireNonNull(currencyType, "currencyType must not be null");
    }

    public String  getInternalAuditCode() { return internalAuditCode; }
    public boolean isTaxable()            { return isTaxable; }
    public String  getCurrencyType()      { return currencyType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FinancialDetails)) return false;
        FinancialDetails other = (FinancialDetails) o;
        return isTaxable == other.isTaxable
            && Objects.equals(internalAuditCode, other.internalAuditCode)
            && Objects.equals(currencyType,      other.currencyType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internalAuditCode, isTaxable, currencyType);
    }

    @Override
    public String toString() {
        return "FinancialDetails{auditCode='" + internalAuditCode
            + "', taxable=" + isTaxable
            + ", currency='" + currencyType + "'}";
    }
}