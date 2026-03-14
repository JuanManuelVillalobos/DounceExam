package com.bank.domain;

import java.util.Objects;

public final class FinancialDetails {
    private final String  internalAuditCode;
    private final boolean isTaxable;
    private final String  currencyType;

    public FinancialDetails(String internalAuditCode, boolean isTaxable, String currencyType) {
        this.internalAuditCode = Objects.requireNonNull(internalAuditCode);
        this.isTaxable         = isTaxable;
        this.currencyType      = Objects.requireNonNull(currencyType);
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

    @Override public int hashCode() { return Objects.hash(internalAuditCode, isTaxable, currencyType); }

    @Override
    public String toString() {
        return "FinancialDetails{auditCode='" + internalAuditCode
            + "', taxable=" + isTaxable
            + ", currency='" + currencyType + "'}";
    }
}