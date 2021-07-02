package de.banking.spl.repository.database;

import de.banking.spl.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static de.banking.spl.model.TransactionType.*;

public class DatabaseTransactionRepository {

    Connection connection;

    public DatabaseTransactionRepository(Connection connection) {
        this.connection = connection;
    }

    public List<Transaction> findByBankAccountId(Long bankAccountId) throws Exception {
        var preparedStatement = connection.prepareStatement("SELECT * FROM transaction WHERE bankaccount_id = ?");
        preparedStatement.setLong(1, bankAccountId);
        var result = preparedStatement.executeQuery();
        List<Transaction> transactions = new ArrayList<>();
        while (result.next()) {
            Long time = result.getTimestamp("time").toInstant().toEpochMilli();
            Long amount = result.getLong("amount");
            String type = result.getString("type");
            boolean failed = result.getBoolean("failed");
            Transaction transaction = new Transaction();
            transaction.setTime(time);
            transaction.setAmount(amount);
            transaction.setTransactionType(valueOf(type));
            transaction.setFailed(failed);
            transactions.add(transaction);
        }
        return transactions;
    }

    public List<Transaction> insertAll(List<Transaction> transactions, Long bankAccountId) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO transaction (time, amount, type, failed, bankaccount_id) VALUES (?, ?, ?, ?, ?)");

        int i = 0;

        for (Transaction transaction : transactions) {
            System.out.println("inserting transaction...");
            preparedStatement.setTimestamp(1, Timestamp.from(Instant.ofEpochMilli(transaction.getTime())));
            preparedStatement.setLong(2, transaction.getAmount());
            preparedStatement.setString(3, transaction.getTransactionType().toString());
            preparedStatement.setBoolean(4, transaction.isFailed());
            preparedStatement.setLong(5, bankAccountId);
            preparedStatement.addBatch();
            i++;

            if (i % 100 == 0 || i == transactions.size()) {
                preparedStatement.executeBatch(); // Execute every 1000 items.
            }
        }
        return transactions;
    }

    public Transaction insert(Transaction transaction, Long bankAccountId) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO transaction (time, amount, type, failed, bankaccount_id) VALUES (?, ?, ?, ?, ?)");

        System.out.println("inserting transaction...");
        preparedStatement.setTimestamp(1, Timestamp.from(Instant.ofEpochMilli(transaction.getTime())));
        preparedStatement.setLong(2, transaction.getAmount());
        preparedStatement.setString(3, transaction.getTransactionType().toString());
        preparedStatement.setBoolean(4, transaction.isFailed());
        preparedStatement.setLong(5, bankAccountId);
        preparedStatement.execute();
        return transaction;
    }
}
