package de.system.banking.repository.database;

import de.system.banking.model.BankAccount;
import de.system.banking.model.Customer;
import de.system.banking.repository.BankAccountRepository;
import de.system.banking.repository.CustomerRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class DatabaseCustomerRepository implements CustomerRepository {

    private Connection connection;

    private BankAccountRepository databaseBankAccountRepository;

    public DatabaseCustomerRepository(Connection connection, BankAccountRepository databaseBankAccountRepository) {
        this.databaseBankAccountRepository = databaseBankAccountRepository;
        this.connection = connection;
    }

    @Override
    public Optional<Customer> findById(Long id) throws Exception {
        var preparedStatement = connection.prepareStatement("SELECT * FROM customer WHERE id = ?");
        preparedStatement.setLong(1, id);
        var result = preparedStatement.executeQuery();
        if (result.next()) {
            String firstName = result.getString("firstName");
            String lastName = result.getString("lastName");
            String passWord = result.getString("passWord");
            Customer customer = new Customer();
            customer.setId(id);
            customer.setFirstName(firstName);
            customer.setLastName(lastName);
            customer.setPassWord(passWord);
            List<BankAccount> bankAccountCollection = databaseBankAccountRepository.findByCustomerId(id);
            customer.setBankAccounts(bankAccountCollection);
            return Optional.of(customer);
        }
        return Optional.empty();
    }

    private void log(String log) {
        System.out.println(log);
    }

    @Override
    public Customer update(Long id, Customer customer) throws Exception {
        Optional<Customer> optionalCustomer = this.findById(id);
        if (optionalCustomer.isEmpty()) {
            log("customer to update not found");
            return null;
        }
        PreparedStatement preparedStatement =
                connection.prepareStatement("UPDATE customer SET firstName = ?, lastName = ?, passWord = ? WHERE id = ?");
        preparedStatement.setString(1, customer.getFirstName());
        preparedStatement.setString(2, customer.getLastName());
        preparedStatement.setString(3, customer.getPassWord());
        preparedStatement.setLong(4, id);
        preparedStatement.execute();
        log("customer updated");
        return customer;
    }

    @Override
    public Customer insert(Customer customer) throws Exception {
        PreparedStatement prepareStatement;
        if (customer.getId() == null) {
            prepareStatement =
                    connection.prepareStatement("INSERT INTO customer (firstName, lastName, passWord) VALUES (?, ?, ?)", RETURN_GENERATED_KEYS);
        } else {
            prepareStatement =
                    connection.prepareStatement("INSERT INTO customer (firstName, lastName, passWord, id ) VALUES (?, ?, ?, ?)", RETURN_GENERATED_KEYS);
            prepareStatement.setLong(4, customer.getId());

        }

        prepareStatement.setString(1, customer.getFirstName());
        prepareStatement.setString(2, customer.getLastName());
        prepareStatement.setString(3, customer.getPassWord());
        prepareStatement.execute();
        ResultSet resultSet = prepareStatement.getGeneratedKeys();
        if (resultSet.next()) {
            long id = resultSet.getLong("id");
            customer.setId(id);
        }
        log("customer inserted");
        return customer;
    }
}
