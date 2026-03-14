package com.bank.domain.src;

import java.util.Objects;

public record FinancialDetails(String internalAuditCode, boolean isTaxable, String currencyType) {
    public FinancialDetails(String internalAuditCode, boolean isTaxable, String currencyType) {
        this.internalAuditCode = Objects.requireNonNull(internalAuditCode);
        this.isTaxable = isTaxable;
        this.currencyType = Objects.requireNonNull(currencyType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FinancialDetails(String auditCode, boolean taxable, String type))) return false;
        return isTaxable == taxable
                && Objects.equals(internalAuditCode, auditCode)
                && Objects.equals(currencyType, type);
    }

    @Override
    public String toString() {
        return "FinancialDetails{auditCode='" + internalAuditCode
                + "', taxable=" + isTaxable
                + ", currency='" + currencyType + "'}";
    }
}