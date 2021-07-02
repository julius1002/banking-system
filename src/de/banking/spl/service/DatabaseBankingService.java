package de.banking.spl.service;

import de.banking.spl.model.BankAccount;
import de.banking.spl.model.Transaction;
import de.banking.spl.repository.BankAccountRepository;
import de.banking.spl.repository.database.DatabaseTransactionRepository;

import java.time.Instant;
import java.util.Optional;

import static de.banking.spl.model.TransactionType.*;

public class DatabaseBankingService extends BankingService {
    private DatabaseTransactionRepository databaseTransactionRepository;

    public DatabaseBankingService(BankAccountRepository bankAccountRepository, DatabaseTransactionRepository databaseTransactionRepository) {
        super(bankAccountRepository);
        this.databaseTransactionRepository = databaseTransactionRepository;
    }

    @Override
    public boolean withDraw(Long bankAccountId, Long amount) throws Exception {
        var optionalBankAccount = bankAccountRepository.findById(bankAccountId);

        var bankAccount = optionalBankAccount.isEmpty() ? null : optionalBankAccount.get();

        if (bankAccount == null) {
            return false;
        }

        var transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTime(Instant.now().toEpochMilli());
        transaction.setTransactionType(WITHDRAW);
        transaction.setFailed(!bankAccount.isOverDraftEligible() && bankAccount.getBalance() - amount < 0);
        boolean failed = transaction.isFailed();

        if (!failed) {
            bankAccount.setBalance(bankAccount.getBalance() - amount);
            this.bankAccountRepository.update(bankAccount);
        }
        this.databaseTransactionRepository.insert(transaction, bankAccountId);
        return !failed;
    }

    @Override
    public boolean deposit(Long bankAccountId, Long amount) throws Exception {
        var optionalBankAccount = bankAccountRepository.findById(bankAccountId);

        var bankAccount = optionalBankAccount.isEmpty() ? null : optionalBankAccount.get();

        if (bankAccount == null) {
            return false;
        }

        var transaction = new Transaction();

        transaction.setAmount(amount);
        transaction.setTime(Instant.now().toEpochMilli());
        transaction.setTransactionType(DEPOSIT);
        bankAccount.setBalance(bankAccount.getBalance() + amount);

        this.bankAccountRepository.update(bankAccount);

        databaseTransactionRepository.insert(transaction, bankAccountId);
        return true;
    }

    @Override
    public boolean transfer(Long originId, Long destinationId, Long amount) throws Exception {

        Optional<BankAccount> bankAccount = bankAccountRepository.findById(originId);
        Optional<BankAccount> destinationBankAccount = bankAccountRepository.findById(destinationId);

        if (bankAccount.isEmpty() || destinationBankAccount.isEmpty() || originId.equals(destinationId)) {
            return false;
        }

        BankAccount origin = bankAccount.get();
        BankAccount destination = destinationBankAccount.get();

        Transaction transaction = new Transaction();
        transaction.setAmount(Math.negateExact(amount));
        transaction.setTime(Instant.now().toEpochMilli());
        transaction.setTransactionType(TRANSFER);
        transaction.setFailed(origin.getBalance() - amount < 0);

        if (origin.getBalance() - amount < 0) {
            return false;
        }
        origin.setBalance(origin.getBalance() - amount);

        destination.setBalance(destination.getBalance() + amount);

        BankAccount updatedOrigin = this.bankAccountRepository.update(origin);
        BankAccount updatedDestination = this.bankAccountRepository.update(destination);

        this.databaseTransactionRepository.insert(transaction, originId);
        this.databaseTransactionRepository.insert(new Transaction(transaction, amount), destinationId);

        return !(updatedOrigin == null || updatedDestination == null);
    }
}
