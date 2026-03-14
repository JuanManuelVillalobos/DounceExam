package com.bank.notification;

import com.bank.domain.SavingsAccount;
import java.util.Objects;

/**
 * Service responsible for all outbound customer notifications.
 *
 * Design decisions:
 *  - Single Responsibility: this class only handles notifications.
 *    It does NOT process payments, access the DB, or perform fraud checks.
 *  - Dependency Inversion: depends on the EmailSender abstraction,
 *    not directly on SendGridMailer. Any mail provider can be injected.
 *  - Constructor injection: dependencies are provided at construction time,
 *    making the class fully testable without a real mail server.
 *
 * In the legacy design, MonolithFintechManager called SendGridMailer directly
 * with a hardcoded dependency. Here, the caller injects the implementation.
 */
public final class NotificationService {

    /** Abstraction over any email-sending provider (SendGrid, SES, SMTP, etc.) */
    public interface EmailSender {
        void send(String to, String subject, String body);
    }

    private final EmailSender emailSender;

    public NotificationService(EmailSender emailSender) {
        this.emailSender = Objects.requireNonNull(emailSender, "emailSender must not be null");
    }

    /**
     * Sends a welcome email when a new account is opened.
     *
     * @param recipientEmail the customer's email address
     * @param account        the newly created account
     */
    public void sendWelcomeEmail(String recipientEmail, SavingsAccount account) {
        Objects.requireNonNull(recipientEmail, "recipientEmail must not be null");
        Objects.requireNonNull(account,        "account must not be null");

        String subject = "Welcome — Your Savings Account Is Ready";
        String body    = buildWelcomeBody(account);
        emailSender.send(recipientEmail, subject, body);
    }

    /**
     * Notifies a customer that a payment has been applied to their account.
     *
     * @param recipientEmail the customer's email address
     * @param account        the account after the payment was applied
     * @param amountApplied  the payment amount
     */
    public void sendPaymentConfirmation(String recipientEmail,
                                        SavingsAccount account,
                                        double amountApplied) {
        Objects.requireNonNull(recipientEmail, "recipientEmail must not be null");
        Objects.requireNonNull(account,        "account must not be null");

        String subject = "Payment Confirmation — Account #" + account.getAuditMetadata().getId();
        String body    = "A payment of "
                       + account.getFinancialDetails().getCurrencyType()
                       + " " + amountApplied
                       + " was applied. New balance: "
                       + account.getBankRecordDetails().getLedgerBalance();
        emailSender.send(recipientEmail, subject, body);
    }

    /**
     * Sends a marketing/promotional email to the customer.
     *
     * @param recipientEmail the customer's email address
     * @param promoMessage   the promotional message body
     */
    public void sendMarketingEmail(String recipientEmail, String promoMessage) {
        Objects.requireNonNull(recipientEmail, "recipientEmail must not be null");
        Objects.requireNonNull(promoMessage,   "promoMessage must not be null");

        emailSender.send(recipientEmail, "A Special Offer Just for You", promoMessage);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String buildWelcomeBody(SavingsAccount account) {
        return "Your new savings account (ID: "
             + account.getAuditMetadata().getId()
             + ") has been opened successfully.\n"
             + "Currency: " + account.getFinancialDetails().getCurrencyType() + "\n"
             + "Interest Rate: " + account.getInterestRate() + "%";
    }
}