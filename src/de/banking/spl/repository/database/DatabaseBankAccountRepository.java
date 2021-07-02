package de.banking.spl.repository.database;

import de.banking.spl.model.BankAccount;
import de.banking.spl.model.Transaction;
import de.banking.spl.repository.BankAccountRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class DatabaseBankAccountRepository implements BankAccountRepository {

    private final Connection connection;
    private DatabaseTransactionRepository dataBaseTransactionRepository;

    public DatabaseBankAccountRepository(Connection connection, DatabaseTransactionRepository dataBaseTransactionRepository) {
        this.connection = connection;
        this.dataBaseTransactionRepository = dataBaseTransactionRepository;
    }

    @Override
    public Optional<BankAccount> findById(Long id) throws Exception {
        var preparedStatement = connection.prepareStatement("SELECT * FROM bankaccount WHERE id = ?");
        preparedStatement.setLong(1, id);
        var result = preparedStatement.executeQuery();
        if (result.next()) {
            Long balance = result.getLong("balance");
            boolean overDraftEligible = result.getBoolean("overDraftEligible");
            List<Transaction> transactionsByBankAccountId = this.dataBaseTransactionRepository.findByBankAccountId(id);
            Long customerId = result.getLong("customer_id");
            BankAccount bankAccount = new BankAccount();
            bankAccount.setBalance(balance);
            bankAccount.setId(id);
            bankAccount.setTransactions(transactionsByBankAccountId);
            bankAccount.setOverDraftEligible(overDraftEligible);
            bankAccount.setCustomer_id(customerId);
            return Optional.of(bankAccount);
        }
        log("bankaccount not found " + id);
        return Optional.empty();
    }

    @Override
    public BankAccount insert(BankAccount bankAccount) throws Exception {
        PreparedStatement prepareStatement =
                connection.prepareStatement("INSERT INTO bankaccount (balance, overDraftEligible, customer_id) VALUES (?, ?, ?)", RETURN_GENERATED_KEYS);
        log("inserting bankaccount");

        prepareStatement.setLong(1, bankAccount.getBalance());
        prepareStatement.setBoolean(2, bankAccount.isOverDraftEligible());
        prepareStatement.setLong(3, bankAccount.getCustomer_id());
        prepareStatement.execute();
        ResultSet resultSet = prepareStatement.getGeneratedKeys();

        if (resultSet.next()) {
            long id = resultSet.getLong("id");
            bankAccount.setId(id);
        }

        List<Transaction> transactions = dataBaseTransactionRepository.insertAll(bankAccount.getTransactions(), bankAccount.getId());
        bankAccount.setTransactions(transactions);
        return bankAccount;
    }

    static void log(String log) {
        System.out.println(log);
    }

    @Override
    public BankAccount update(BankAccount bankAccount) throws Exception {
        Long bankAccountId = bankAccount.getId();
        Optional<BankAccount> optionalBankAccount = findById(bankAccountId);

        if (optionalBankAccount.isEmpty()) {
            log("no bankaccount found with id " + bankAccountId);
            return null;
        }
        log("updating bankaccount, id: " + bankAccountId);
        PreparedStatement preparedStatement =
                connection.prepareStatement("UPDATE bankaccount SET balance = ?, overDraftEligible = ? WHERE id = ?");
        preparedStatement.setLong(1, bankAccount.getBalance());
        preparedStatement.setBoolean(2, bankAccount.isOverDraftEligible());
        preparedStatement.setLong(3, bankAccountId);
        log("new Balance: " + bankAccount.getBalance());

        preparedStatement.execute();
        return bankAccount;
    }

    public List<BankAccount> findByCustomerId(Long customerId) throws Exception {
        List<BankAccount> bankAccounts = new ArrayList<>();
        var preparedStatement = connection.prepareStatement("SELECT * FROM bankaccount WHERE customer_id = ?", RETURN_GENERATED_KEYS);
        preparedStatement.setLong(1, customerId);
        var result = preparedStatement.executeQuery();
        while (result.next()) {
            log("bankaccount found for customer: " + customerId);
            Long balance = result.getLong("balance");
            boolean overDraftEligible = result.getBoolean("overDraftEligible");
            long bankAccountId = result.getLong("id");
            List<Transaction> transactionsByBankAccountId = this.dataBaseTransactionRepository.findByBankAccountId(bankAccountId);
            BankAccount bankAccount = new BankAccount();
            bankAccount.setBalance(balance);
            bankAccount.setId(bankAccountId);
            bankAccount.setTransactions(transactionsByBankAccountId);
            bankAccount.setOverDraftEligible(overDraftEligible);
            bankAccounts.add(bankAccount);
        }
        return bankAccounts;
    }
}
