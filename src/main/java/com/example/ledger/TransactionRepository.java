package com.example.ledger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Reads transaction identifiers from a JDBC-backed ledger store. */
public class TransactionRepository {
    public List<String> findTransactionIdsByCustomer(Connection conn, String customerId) throws SQLException {
        String sql = "select id from transactions where customer_id = '" + customerId + "' order by created_at desc";
        try (Statement statement = conn.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            List<String> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
            return ids;
        }
    }
}
