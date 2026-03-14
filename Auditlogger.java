package com.bank.audit;

import com.bank.domain.SavingsAccount;

import java.time.Instant;
import java.util.Objects;

/**
 * Service responsible for writing immutable audit trail entries.
 *
 * Design decisions:
 *  - Single Responsibility: only handles audit logging.
 *    No payments, no mailing, no fraud decisions.
 *  - Dependency Inversion: depends on the AuditSink abstraction.
 *    Concrete implementations can write to a database, a file,
 *    a cloud logging service (CloudWatch, Datadog), etc.
 *  - Every log entry is stamped with an ISO-8601 timestamp
 *    generated at call time to ensure auditability.
 *  - Sensitive fields (SSN) are never written to the audit log.
 *
 * In the legacy design, MonolithFintechManager.logAuditTrail(String msg)
 * accepted a raw string with no structure. Here, log entries are
 * typed and contextualized per operation.
 */
public final class AuditLogger {

    /** Abstraction over the destination for audit records. */
    public interface AuditSink {
        /**
         * Persists a single audit log entry.
         *
         * @param entry the fully formed audit entry
         */
        void write(AuditEntry entry);
    }

    /** Immutable audit log record. */
    public static final class AuditEntry {

        private final String    timestamp;
        private final int       accountId;
        private final String    action;
        private final String    details;

        public AuditEntry(String timestamp, int accountId, String action, String details) {
            this.timestamp = Objects.requireNonNull(timestamp);
            this.accountId = accountId;
            this.action    = Objects.requireNonNull(action);
            this.details   = Objects.requireNonNull(details);
        }

        public String getTimestamp() { return timestamp; }
        public int    getAccountId() { return accountId; }
        public String getAction()    { return action; }
        public String getDetails()   { return details; }

        @Override
        public String toString() {
            return "[" + timestamp + "] accountId=" + accountId
                + " action='" + action + "' details='" + details + "'";
        }
    }

    private final AuditSink sink;

    public AuditLogger(AuditSink sink) {
        this.sink = Objects.requireNonNull(sink, "sink must not be null");
    }

    /**
     * Logs that an account was created.
     *
     * @param account the newly created account
     */
    public void logAccountCreated(SavingsAccount account) {
        Objects.requireNonNull(account, "account must not be null");
        sink.write(new AuditEntry(
            now(),
            account.getAuditMetadata().getId(),
            "ACCOUNT_CREATED",
            "currency=" + account.getFinancialDetails().getCurrencyType()
          + " rate="     + account.getInterestRate()
        ));
    }

    /**
     * Logs a payment event (deposit or withdrawal) on an account.
     *
     * @param account the account after the payment was applied
     * @param action  a label such as "DEPOSIT" or "WITHDRAWAL"
     * @param amount  the payment amount
     */
    public void logPayment(SavingsAccount account, String action, double amount) {
        Objects.requireNonNull(account, "account must not be null");
        Objects.requireNonNull(action,  "action must not be null");
        sink.write(new AuditEntry(
            now(),
            account.getAuditMetadata().getId(),
            action,
            "amount=" + amount + " newBalance=" + account.getBankRecordDetails().getLedgerBalance()
        ));
    }

    /**
     * Logs a custom message with the given action label.
     *
     * @param accountId the account this entry relates to
     * @param action    the action label (e.g., "FRAUD_ALERT", "ACCOUNT_CLOSED")
     * @param details   free-form details; must NOT contain SSN or other PII
     */
    public void log(int accountId, String action, String details) {
        Objects.requireNonNull(action,  "action must not be null");
        Objects.requireNonNull(details, "details must not be null");
        sink.write(new AuditEntry(now(), accountId, action, details));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String now() {
        return Instant.now().toString(); // ISO-8601 UTC
    }
}