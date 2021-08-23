package de.system.banking;

import java.net.http.HttpResponse;
import java.sql.*;
import java.time.Instant;
import java.util.*;

import com.google.gson.*;
import de.system.banking.model.BankAccount;
import de.system.banking.model.Customer;
import de.system.banking.model.BankingTransaction;
import de.system.banking.repository.BankAccountRepository;
import de.system.banking.repository.CustomerRepository;
import de.system.banking.repository.inmemory.InMemoryBankAccountRepository;
import de.system.banking.repository.inmemory.InMemoryCustomerRepository;
import de.system.banking.repository.database.DatabaseBankAccountRepository;
import de.system.banking.repository.database.DatabaseCustomerRepository;
import de.system.banking.repository.database.DatabaseBankingTransactionRepository;
import de.system.banking.service.*;

import static java.lang.System.*;

public class App {

    public static void main(String[] args) throws Exception {

        boolean enableDatabase = true;

        var customers = new ArrayList<Customer>();
        var bankAccounts = new ArrayList<BankAccount>();

        var preRegisteredCustomer = new Customer(1L, "Erika", "Musterfrau", "secret", new ArrayList<>());
        var preRegisteredBankAccount = new BankAccount(123L, 5000L);
        preRegisteredBankAccount.setCustomer_id(preRegisteredCustomer.getId());
        bankAccounts.add(preRegisteredBankAccount);
        preRegisteredCustomer.getBankAccounts().add(preRegisteredBankAccount);

        customers.add(preRegisteredCustomer);

        CustomerRepository customerRepository;
        BankAccountRepository bankAccountRepository;

        BankingService bankingService;

        /*
        InMemory
         */
        if (!enableDatabase) {
            customerRepository = new InMemoryCustomerRepository(customers);
            bankAccountRepository = new InMemoryBankAccountRepository(bankAccounts);
            bankingService = new BankingService(bankAccountRepository);
        }
        /*
        DB
         */
        else {
            var connection = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
            createTables(connection);

            var databaseBankingTransactionRepository = new DatabaseBankingTransactionRepository(connection);
            bankAccountRepository = new DatabaseBankAccountRepository(connection, databaseBankingTransactionRepository);
            customerRepository = new DatabaseCustomerRepository(connection, bankAccountRepository);

            preRegisteredCustomer.setFirstName(preRegisteredCustomer.getFirstName() + "@DB");
            var insert = customerRepository.insert(preRegisteredCustomer);
            bankAccounts.get(0).setCustomer_id(insert.getId());
            bankAccountRepository.insert(bankAccounts.get(0));

            bankingService = new DatabaseBankingService(bankAccountRepository, databaseBankingTransactionRepository);

        }

        var customerService = new CustomerService(customerRepository);

        /*
        Bootstrapping
         */
        var scanner = new Scanner(in);

        while (true) {
            log("Welcome to your Banking System.");
            log("Please enter a number to log in or type in 0 to register");

            log("Type 9 to login with an Identity Provider (OpenID)");
            var loginOrSignUp = Long.parseLong(scanner.nextLine());

            Customer foundCustomer = null;
            if (loginOrSignUp == 9) {

                //oidc start
                int status = 0;
                while (status != 200) {

                    log("Type your username");
                    String username = scanner.nextLine(); //value: m.mustermann@example.com

                    log("Type in your password");
                    var password = scanner.nextLine(); //value: maxssecret

                    var authorizationServerUri = "http://localhost:3000";

                    var openIdConnectService = new ResourceOwnerPasswordCredentialsService(authorizationServerUri);

                    var tokenResponse = openIdConnectService.requestAccessToken(username, password, "profile email");

                    status = tokenResponse.statusCode();

                    if (status == 200) {

                        var gson = new GsonBuilder().create();
                        var tokenBody = gson.fromJson(tokenResponse.body(), Map.class);

                        var token = (String) tokenBody.get("token");

                        var userInfoResponse = openIdConnectService.requestUserInfo(token);

                        if (userInfoResponse.statusCode() == 200) {
                            var map = gson.fromJson(userInfoResponse.body(), Map.class);

                            long id = (long) Double.parseDouble(map.get("id").toString());

                            var optionalCustomer = customerRepository.findById(id);
                            if (optionalCustomer.isEmpty()) {
                                foundCustomer = new Customer();
                                foundCustomer.setFirstName((String) map.get("firstName"));
                                foundCustomer.setLastName((String) map.get("lastName"));
                                foundCustomer.setPassWord(password);
                                foundCustomer.setId(id);
                                var registeredCustomer = customerService.insertCustomer(foundCustomer);
                                var insertedBankAccount = bankAccountRepository.insert(new BankAccount(0L, Collections.emptyList(), false, registeredCustomer.getId()));
                                var newBankAccounts = new ArrayList<BankAccount>();
                                newBankAccounts.add(insertedBankAccount);
                                foundCustomer.setBankAccounts(newBankAccounts);

                                log("Hello " + map.get("firstName") + " " + map.get("lastName") + " Thank you for being our customer!\nYour ID is:" + registeredCustomer.getId()
                                        + "\nWe have opened a bankaccount with the ID: " + registeredCustomer.getBankAccounts().get(0).getId());
                            } else {
                                foundCustomer = optionalCustomer.get();

                                log("Hello " + foundCustomer.getFirstName() + " " + foundCustomer.getLastName() + " Thank you for being our customer!");
                            }
                        }

                    } else {
                        log("Wrong credentials, please try again");
                    }
                }
                //oidc end
            } else if (loginOrSignUp == 0) {
                log("Type your firstname\nType -1 to cancel");
                var input = scanner.nextLine();
                if (input.equals("-1")) {
                    log("Processs canceled");
                } else {
                    var newCustomer = new Customer();

                    newCustomer.setFirstName(input);

                    log("Type your lastname");
                    var lastName = scanner.nextLine();
                    newCustomer.setLastName(lastName);

                    log("Type in a password");
                    var passWord = scanner.nextLine();
                    newCustomer.setPassWord(passWord);

                    var registeredCustomer = customerService.insertCustomer(newCustomer);
                    var insertedBankAccount = bankAccountRepository.insert(new BankAccount(0L, Collections.emptyList(), false, registeredCustomer.getId()));
                    var newBankAccounts = new ArrayList<BankAccount>();
                    newBankAccounts.add(insertedBankAccount);
                    registeredCustomer.setBankAccounts(newBankAccounts);

                    log("Thank you for being our customer!\nYour ID is:" + registeredCustomer.getId()
                            + "\nWe have opened a bankaccount with the ID: " + registeredCustomer.getBankAccounts().get(0).getId());
                }
            }


            log("Please enter your User Id");
            var customerId = Long.parseLong(scanner.nextLine());

            while (customerRepository.findById(customerId).isEmpty()) {
                log("No User with that Id found\n Please try again");
                customerId = Long.parseLong(scanner.nextLine());
            }

            foundCustomer = customerRepository.findById(customerId).get();

            log("Hello " + foundCustomer.getFirstName() + " " + foundCustomer.getLastName() + "!\nPlease enter your Password to continue.");
            var passWord = scanner.nextLine();

            while (!foundCustomer.getPassWord().equals(passWord)) {
                log("Wrong password, please try again");
                passWord = scanner.nextLine();
            }
            log("Successfully logged in");

            int numberOfBankAccounts = foundCustomer.getBankAccounts().size();

            log(numberOfBankAccounts > 1 ? "You have " + numberOfBankAccounts + " bankaccounts"
                    : "You have " + numberOfBankAccounts + " bankaccount\nType in the bankaccount number");

            for (BankAccount bankAccount : foundCustomer.getBankAccounts()) {
                log(bankAccount.getId().toString());
            }
            var bankAccountId = Long.parseLong(scanner.nextLine());

            while (!(bankAccountRepository.findById(bankAccountId).isPresent() && foundCustomer.getId().equals(bankAccountRepository.findById(bankAccountId).get().getCustomer_id()))) {
                log("No bankaccount with that Id found\nPlease try again");
                bankAccountId = Long.parseLong(scanner.nextLine());
            }

            var choice = "";

            while (!"-1".equals(choice)) {

                log("Choose your option\n0: View Balance\n1: Withdraw\n2: Deposit");
                log("3: Update User details");
                log("4: View Transactions");

                log("5: Print transactions to file");
                log("6: Transfer money");

                log("-1: End session");
                choice = scanner.nextLine();

                if (choice.equals("0")) {
                    log("Balance: " + bankAccountRepository.findById(bankAccountId).get().getBalance());
                }
                if (choice.equals("1")) {
                    log("How much do you want to withdraw?\nType -1 to cancel");
                    Long amount = scanner.nextLong();
                    if (amount == -1) {
                        log("Transaction canceled");
                    } else {
                        bankingService.withDraw(bankAccountId, amount);
                        log("You successfully withdrawed " + amount + " !");
                    }
                }

                if (choice.equals("2")) {
                    log("How much do you want to deposit?\nType -1 to cancel");
                    long amount = scanner.nextLong();
                    if (amount == -1) {
                        log("Transaction canceled");
                    } else {
                        bankingService.deposit(bankAccountId, amount);
                        log("You successfully deposited " + amount + " !");
                    }
                }
                if (choice.equals("3")) {
                    log("Type your firstname\nType -1 to cancel");
                    String input = scanner.nextLine();
                    if (input.equals("-1")) {
                        log("Processs canceled");
                    } else {
                        Customer updateCustomer = new Customer();
                        updateCustomer.setFirstName(input);
                        log("Type your lastname");
                        String lastName = scanner.nextLine();
                        updateCustomer.setLastName(lastName);
                        foundCustomer = customerService.changeUserDetails(foundCustomer.getId(), updateCustomer);
                        log("Userdetails succesfully updated");
                        log(foundCustomer.getFirstName() + " " + foundCustomer.getLastName());
                    }
                }
                if (choice.equals("4")) {
                    List<BankingTransaction> bankingTransactions = bankAccountRepository.findById(bankAccountId).get().getTransactions();
                    log("You had " + bankingTransactions.size() + " transactions in the past");
                    if (bankingTransactions.size() > 0) log("Date, Type, Amount, Failed\n-----------------------");
                    for (BankingTransaction bankingTransaction : bankingTransactions) {
                        log(Instant.ofEpochMilli(bankingTransaction.getTime()) +
                                ", " + bankingTransaction.getTransactionType() + ", " +
                                bankingTransaction.getAmount() + ", " + bankingTransaction.isFailed());
                    }
                }
                var bankingTransactionPrintingService = new BankingTransactionPrintingService("./files");

                if (choice.equals("5")) {
                    bankingTransactionPrintingService.createFile(bankAccountRepository.findById(bankAccountId).get().getTransactions(), foundCustomer.getFirstName() + "_" + foundCustomer.getLastName());
                    log("Your file is present in the /files directory");
                }
                if (choice.equals("6")) {
                    log("Type in your bankaccount number");
                    Long bankAccountNumber = scanner.nextLong();
                    log("Type in the destination bankaccount number");
                    Long destinationBankAccountNumber = scanner.nextLong();
                    log("Type in the amount");
                    Long amount = scanner.nextLong();
                    if (bankingService.transfer(bankAccountNumber, destinationBankAccountNumber, amount)) {
                        log("Transfer was successful!");
                    } else {
                        log("Transfer failed");
                    }
                }
            }
            log("Bye " + foundCustomer.getFirstName() + " " + foundCustomer.getLastName() + ", hope to see you soon!\n");
        }
    }

    static void log(String log) {
        out.println(log);
    }

    private static void createTables(Connection connection) throws SQLException {
        var ddl = "DROP TABLE IF EXISTS transaction cascade; DROP TABLE IF EXISTS bankaccount cascade; DROP TABLE IF EXISTS customer cascade;" +
                "CREATE TABLE IF NOT EXISTS customer (id int AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR (255), lastName VARCHAR (255), passWord VARCHAR(255));" +
                "CREATE TABLE IF NOT EXISTS bankaccount (id int AUTO_INCREMENT PRIMARY KEY, balance int, overDraftEligible bit, customer_id int, FOREIGN KEY (customer_id) REFERENCES customer(id));" +
                "CREATE TABLE IF NOT EXISTS transaction (id int AUTO_INCREMENT PRIMARY KEY, time TIMESTAMP, amount int, type VARCHAR(255), failed bit, bankaccount_id int, FOREIGN KEY (bankaccount_id) REFERENCES bankaccount(id));";
        connection.prepareStatement(ddl).executeUpdate();
    }
}
