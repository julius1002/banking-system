package de.system.banking.repository.database;

import de.system.banking.model.BankingTransaction;
import de.system.banking.model.BankingTransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DatabaseBankingTransactionRepository {

    Connection connection;

    public DatabaseBankingTransactionRepository(Connection connection) {
        this.connection = connection;
    }

    public List<BankingTransaction> findByBankAccountId(Long bankAccountId) throws Exception {
        var preparedStatement = connection.prepareStatement("SELECT * FROM transaction WHERE bankaccount_id = ?");
        preparedStatement.setLong(1, bankAccountId);
        var result = preparedStatement.executeQuery();
        List<BankingTransaction> bankingTransactions = new ArrayList<>();
        while (result.next()) {
            Long time = result.getTimestamp("time").toInstant().toEpochMilli();
            Long amount = result.getLong("amount");
            String type = result.getString("type");
            boolean failed = result.getBoolean("failed");
            BankingTransaction bankingTransaction = new BankingTransaction();
            bankingTransaction.setTime(time);
            bankingTransaction.setAmount(amount);
            bankingTransaction.setTransactionType(BankingTransactionType.valueOf(type));
            bankingTransaction.setFailed(failed);
            bankingTransactions.add(bankingTransaction);
        }
        return bankingTransactions;
    }

    public List<BankingTransaction> insertAll(List<BankingTransaction> bankingTransactions, Long bankAccountId) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO transaction (time, amount, type, failed, bankaccount_id) VALUES (?, ?, ?, ?, ?)");

        int i = 0;

        for (BankingTransaction bankingTransaction : bankingTransactions) {
            System.out.println("inserting transaction...");
            preparedStatement.setTimestamp(1, Timestamp.from(Instant.ofEpochMilli(bankingTransaction.getTime())));
            preparedStatement.setLong(2, bankingTransaction.getAmount());
            preparedStatement.setString(3, bankingTransaction.getTransactionType().toString());
            preparedStatement.setBoolean(4, bankingTransaction.isFailed());
            preparedStatement.setLong(5, bankAccountId);
            preparedStatement.addBatch();
            i++;

            if (i % 100 == 0 || i == bankingTransactions.size()) {
                preparedStatement.executeBatch(); // Execute every 1000 items.
            }
        }
        return bankingTransactions;
    }

    public BankingTransaction insert(BankingTransaction bankingTransaction, Long bankAccountId) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO transaction (time, amount, type, failed, bankaccount_id) VALUES (?, ?, ?, ?, ?)");

        System.out.println("inserting transaction...");
        preparedStatement.setTimestamp(1, Timestamp.from(Instant.ofEpochMilli(bankingTransaction.getTime())));
        preparedStatement.setLong(2, bankingTransaction.getAmount());
        preparedStatement.setString(3, bankingTransaction.getTransactionType().toString());
        preparedStatement.setBoolean(4, bankingTransaction.isFailed());
        preparedStatement.setLong(5, bankAccountId);
        preparedStatement.execute();
        return bankingTransaction;
    }
}
