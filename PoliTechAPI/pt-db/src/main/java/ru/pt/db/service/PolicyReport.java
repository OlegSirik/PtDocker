package ru.pt.db.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ru.pt.api.dto.sales.QuoteDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Reporting helper that executes policy search query and maps results to QuoteDto.
 *
 * The SQL is based on the native query defined in PolicyIndexRepository.
 */
@Repository
public class PolicyReport {

    private static final String POLICY_REPORT_SQL = """
        WITH RECURSIVE account_tree AS (
            SELECT id FROM acc_accounts WHERE id = ?
            UNION ALL
            SELECT a.id FROM acc_accounts a
            JOIN account_tree at ON a.parent_id = at.id
        )
        SELECT 
            p.id,
            p.draft_id,
            p.policy_nr,
            p.product_code,
            p.create_date,
            p.issue_date,
            p.payment_date,
            p.start_date,
            p.end_date,
            p.policy_status, 
            p.user_account_id,
            p.client_account_id,
            p.version_status,
            p.payment_order_id,
            p.ph_digest,
            p.io_digest,
            p.premium::text,
            p.agent_kv_percent::text,
            p.agent_kv_amount::text
        FROM policy_index p
        JOIN account_tree at ON p.user_account_id = at.id
        WHERE ( p.policy_nr LIKE ? OR p.ph_digest LIKE ? )
        ORDER BY p.policy_nr
        """;

    private final JdbcTemplate jdbcTemplate;

    public PolicyReport(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Execute report query and map rows to QuoteDto list.
     *
     * @param accountId account id (root of the recursive tree)
     * @param qstr search string used in LIKE conditions
     */
    public List<QuoteDto> findPoliciesByAccountRecursive(long accountId, String qstr) {
        String like = "%" + (qstr != null ? qstr : "") + "%";
        return jdbcTemplate.query(
            POLICY_REPORT_SQL,
            new Object[]{accountId, like, like},
            new QuoteRowMapper()
        );
    }

    private static class QuoteRowMapper implements RowMapper<QuoteDto> {

        @Override
        public QuoteDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            ZonedDateTime createDate = toZonedDateTime(rs.getTimestamp(5));
            ZonedDateTime issueDate = toZonedDateTime(rs.getTimestamp(6));
            ZonedDateTime paymentDate = toZonedDateTime(rs.getTimestamp(7));
            ZonedDateTime startDate = toZonedDateTime(rs.getTimestamp(8));
            ZonedDateTime endDate = toZonedDateTime(rs.getTimestamp(9));

            String issueTimezone = ZoneId.systemDefault().getId();

            return new QuoteDto(
                rs.getString(1),                 // id
                rs.getString(2),                 // draftId
                rs.getString(3),                 // policyNr
                rs.getString(4),                 // productCode
                null,                            // insCompany (not present)
                createDate,                      // createDate
                issueDate,                       // issueDate
                issueTimezone,                   // issueTimezone
                paymentDate,                     // paymentDate
                startDate,                       // startDate
                endDate,                         // endDate
                rs.getString(10),                // policyStatus
                rs.getString(15),                // phDigest
                rs.getString(16),                // ioDigest
                rs.getString(17),                // premium (text)
                "account_id",                    // agentDigest (placeholder)
                rs.getString(18),                // agentKvPrecent
                rs.getString(19),                // agentKvAmount
                Boolean.TRUE,                    // comand1
                Boolean.FALSE,                   // comand2
                Boolean.FALSE,                   // comand3
                Boolean.FALSE,                   // comand4
                Boolean.FALSE,                   // comand5
                Boolean.FALSE,                   // comand6
                Boolean.FALSE,                   // comand7
                Boolean.FALSE,                   // comand8
                Boolean.FALSE                    // comand9
            );
        }

        private static ZonedDateTime toZonedDateTime(Timestamp ts) {
            if (ts == null) {
                return null;
            }
            return ts.toInstant().atZone(ZoneId.systemDefault());
        }
    }
}
