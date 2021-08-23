package de.system.banking.service;

import de.system.banking.model.BankAccount;
import de.system.banking.model.BankingTransaction;
import de.system.banking.repository.BankAccountRepository;
import de.system.banking.repository.database.DatabaseBankingTransactionRepository;

import java.time.Instant;
import java.util.Optional;

import static de.system.banking.model.BankingTransactionType.*;

public class DatabaseBankingService extends BankingService {
    private DatabaseBankingTransactionRepository databaseBankingTransactionRepository;

    public DatabaseBankingService(BankAccountRepository bankAccountRepository, DatabaseBankingTransactionRepository databaseBankingTransactionRepository) {
        super(bankAccountRepository);
        this.databaseBankingTransactionRepository = databaseBankingTransactionRepository;
    }

    @Override
    public boolean withDraw(Long bankAccountId, Long amount) throws Exception {
        var optionalBankAccount = bankAccountRepository.findById(bankAccountId);

        var bankAccount = optionalBankAccount.isEmpty() ? null : optionalBankAccount.get();

        if (bankAccount == null) {
            return false;
        }

        var transaction = new BankingTransaction();
        transaction.setAmount(amount);
        transaction.setTime(Instant.now().toEpochMilli());
        transaction.setTransactionType(WITHDRAW);
        transaction.setFailed(!bankAccount.isOverDraftEligible() && bankAccount.getBalance() - amount < 0);
        boolean failed = transaction.isFailed();

        if (!failed) {
            bankAccount.setBalance(bankAccount.getBalance() - amount);
            this.bankAccountRepository.update(bankAccount);
        }
        this.databaseBankingTransactionRepository.insert(transaction, bankAccountId);
        return !failed;
    }

    @Override
    public boolean deposit(Long bankAccountId, Long amount) throws Exception {
        var optionalBankAccount = bankAccountRepository.findById(bankAccountId);

        var bankAccount = optionalBankAccount.isEmpty() ? null : optionalBankAccount.get();

        if (bankAccount == null) {
            return false;
        }

        var transaction = new BankingTransaction();

        transaction.setAmount(amount);
        transaction.setTime(Instant.now().toEpochMilli());
        transaction.setTransactionType(DEPOSIT);
        bankAccount.setBalance(bankAccount.getBalance() + amount);

        this.bankAccountRepository.update(bankAccount);

        databaseBankingTransactionRepository.insert(transaction, bankAccountId);
        return true;
    }

    @Override
    public boolean transfer(Long originId, Long destinationId, Long amount) throws Exception {

        var bankAccount = bankAccountRepository.findById(originId);
        var destinationBankAccount = bankAccountRepository.findById(destinationId);

        if (bankAccount.isEmpty() || destinationBankAccount.isEmpty() || originId.equals(destinationId)) {
            return false;
        }

        BankAccount origin = bankAccount.get();
        BankAccount destination = destinationBankAccount.get();

        BankingTransaction bankingTransaction = new BankingTransaction();
        bankingTransaction.setAmount(Math.negateExact(amount));
        bankingTransaction.setTime(Instant.now().toEpochMilli());
        bankingTransaction.setTransactionType(TRANSFER);
        bankingTransaction.setFailed(origin.getBalance() - amount < 0);

        if (origin.getBalance() - amount < 0) {
            return false;
        }
        origin.setBalance(origin.getBalance() - amount);

        destination.setBalance(destination.getBalance() + amount);

        var updatedOrigin = this.bankAccountRepository.update(origin);
        var updatedDestination = this.bankAccountRepository.update(destination);

        this.databaseBankingTransactionRepository.insert(bankingTransaction, originId);
        this.databaseBankingTransactionRepository.insert(new BankingTransaction(bankingTransaction, amount), destinationId);

        return !(updatedOrigin == null || updatedDestination == null);
    }
}
