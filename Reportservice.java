package com.bank.report;

import com.bank.domain.SavingsAccount;

import java.util.List;
import java.util.Objects;

/**
 * Service responsible for generating account reports and statements.
 *
 * Design decisions:
 *  - Single Responsibility: only handles report generation.
 *    No DB access, no mailing, no fraud checks.
 *  - Dependency Inversion: depends on the ReportRenderer abstraction,
 *    not on a concrete PDF library. Any renderer (PDF, HTML, CSV) can be injected.
 *  - The generated report content is built as a plain String first,
 *    then passed to the renderer — keeping business logic separate from
 *    rendering technology.
 *
 * In the legacy design, MonolithFintechManager.generatePDFMonthlyReport()
 * was tightly coupled to a specific output format. Here, the format is
 * pluggable via the injected ReportRenderer.
 */
public final class ReportService {

    /** Abstraction over any report rendering technology (PDF, HTML, CSV, etc.) */
    public interface ReportRenderer {
        /**
         * Renders the given content into the target format.
         *
         * @param title   the report title
         * @param content the report body content
         * @return the rendered output as a byte array (e.g., PDF bytes)
         */
        byte[] render(String title, String content);
    }

    private final ReportRenderer renderer;

    public ReportService(ReportRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer must not be null");
    }

    /**
     * Generates a monthly account statement for the given account.
     *
     * @param account the account for which to generate the report
     * @param month   the month descriptor (e.g., "March 2026")
     * @return the rendered report as a byte array
     */
    public byte[] generateMonthlyStatement(SavingsAccount account, String month) {
        Objects.requireNonNull(account, "account must not be null");
        Objects.requireNonNull(month,   "month must not be null");

        String title   = "Monthly Statement — Account #"
                       + account.getAuditMetadata().getId()
                       + " — " + month;
        String content = buildStatementContent(account, month);

        return renderer.render(title, content);
    }

    /**
     * Generates a transaction history report for the given account.
     *
     * @param account      the account whose transactions to report
     * @param transactions the specific transactions to include
     * @return the rendered report as a byte array
     */
    public byte[] generateTransactionHistory(SavingsAccount account,
                                              List<String> transactions) {
        Objects.requireNonNull(account,      "account must not be null");
        Objects.requireNonNull(transactions, "transactions must not be null");

        String title   = "Transaction History — Account #" + account.getAuditMetadata().getId();
        String content = buildTransactionContent(account, transactions);

        return renderer.render(title, content);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String buildStatementContent(SavingsAccount account, String month) {
        StringBuilder sb = new StringBuilder();
        sb.append("Period: ").append(month).append("\n");
        sb.append("Currency: ").append(account.getFinancialDetails().getCurrencyType()).append("\n");
        sb.append("Closing Balance: ").append(account.getBankRecordDetails().getLedgerBalance()).append("\n");
        sb.append("Interest Rate: ").append(account.getInterestRate()).append("%\n");
        sb.append("Taxable: ").append(account.getFinancialDetails().isTaxable()).append("\n");
        sb.append("\nTransactions this period:\n");
        account.getTransactions().forEach(tx -> sb.append("  - ").append(tx).append("\n"));
        return sb.toString();
    }

    private String buildTransactionContent(SavingsAccount account, List<String> transactions) {
        StringBuilder sb = new StringBuilder();
        sb.append("Account ID: ").append(account.getAuditMetadata().getId()).append("\n");
        sb.append("Total Transactions: ").append(transactions.size()).append("\n\n");
        transactions.forEach(tx -> sb.append("  ").append(tx).append("\n"));
        return sb.toString();
    }
}