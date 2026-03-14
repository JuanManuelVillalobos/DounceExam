package com.bank.fraud;

import com.bank.domain.SavingsAccount;

import java.util.Objects;

/**
 * Service responsible for fraud detection and alerting.
 *
 * Design decisions:
 *  - Single Responsibility: only handles fraud-related logic.
 *  - Dependency Inversion: depends on the FraudAlertPublisher abstraction.
 *    The concrete implementation could publish to a message queue,
 *    a REST endpoint, an SMS gateway, etc.
 *  - All fraud rules are encapsulated here, making them independently
 *    testable without touching payments, reports, or notifications.
 *
 * In the legacy design, MonolithFintechManager.triggerFraudAlert() was a
 * void method with no clear rules. Here, fraud evaluation logic is
 * explicit, rule-driven, and auditable.
 */
public final class FraudService {

    /** Abstraction for publishing fraud alerts to downstream systems. */
    public interface FraudAlertPublisher {
        /**
         * Publishes a fraud alert for the given account.
         *
         * @param accountId the ID of the flagged account
         * @param reason    a human-readable reason describing the anomaly
         */
        void publish(int accountId, String reason);
    }

    /** Threshold above which a single transaction is considered suspicious. */
    private static final double HIGH_VALUE_THRESHOLD = 10_000.0;

    /** Threshold above which the transaction volume per session is suspicious. */
    private static final int HIGH_VOLUME_THRESHOLD = 20;

    private final FraudAlertPublisher alertPublisher;

    public FraudService(FraudAlertPublisher alertPublisher) {
        this.alertPublisher = Objects.requireNonNull(alertPublisher, "alertPublisher must not be null");
    }

    /**
     * Evaluates a proposed transaction amount against known fraud rules.
     * Publishes an alert if any rule is triggered.
     *
     * @param account           the account on which the transaction is being applied
     * @param transactionAmount the proposed transaction amount
     * @return true if the transaction appears fraudulent; false otherwise
     */
    public boolean evaluate(SavingsAccount account, double transactionAmount) {
        Objects.requireNonNull(account, "account must not be null");

        if (isHighValueTransaction(transactionAmount)) {
            alertPublisher.publish(
                account.getAuditMetadata().getId(),
                "High-value transaction detected: amount=" + transactionAmount
            );
            return true;
        }

        if (isHighVolumeActivity(account)) {
            alertPublisher.publish(
                account.getAuditMetadata().getId(),
                "High-volume activity: txCount=" + account.getTransactions().size()
            );
            return true;
        }

        return false;
    }

    /**
     * Directly triggers a manual fraud alert for the given account.
     * Used when external signals (e.g., customer report) indicate fraud.
     *
     * @param account the account to flag
     * @param reason  the reason for the manual alert
     */
    public void triggerManualAlert(SavingsAccount account, String reason) {
        Objects.requireNonNull(account, "account must not be null");
        Objects.requireNonNull(reason,  "reason must not be null");

        alertPublisher.publish(account.getAuditMetadata().getId(), "MANUAL: " + reason);
    }

    // ── Private fraud rules ───────────────────────────────────────────────────

    private boolean isHighValueTransaction(double amount) {
        return amount > HIGH_VALUE_THRESHOLD;
    }

    private boolean isHighVolumeActivity(SavingsAccount account) {
        return account.getTransactions().size() > HIGH_VOLUME_THRESHOLD;
    }
}