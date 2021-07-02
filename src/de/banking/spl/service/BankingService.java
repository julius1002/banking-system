package de.banking.spl.service;

import java.time.Instant;
import java.util.Optional;

import de.banking.spl.model.BankAccount;
import de.banking.spl.model.Transaction;
import de.banking.spl.repository.BankAccountRepository;

import static de.banking.spl.model.TransactionType.*;

public class BankingService {

    protected BankAccountRepository bankAccountRepository;

    public BankingService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

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
        bankAccount.getTransactions().add(transaction);
        boolean failed = transaction.isFailed();

        if (!failed) {
            bankAccount.setBalance(bankAccount.getBalance() - amount);
            bankAccountRepository.update(bankAccount);
        }

        return !failed;
    }

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
        bankAccount.getTransactions().add(transaction);
        bankAccount.setBalance(bankAccount.getBalance() + amount);
        return true;
    }

    public boolean transfer(Long originId, Long destinationId, Long amount) throws Exception {

        Optional<BankAccount> bankAccount = bankAccountRepository.findById(originId);
        Optional<BankAccount> destinationBankAccount = bankAccountRepository.findById(destinationId);

        if (bankAccount.isEmpty() || destinationBankAccount.isEmpty()) {
            return false;
        }

        BankAccount origin = bankAccount.get();
        BankAccount destination = destinationBankAccount.get();

        Transaction transaction = new Transaction();
        transaction.setAmount(Math.negateExact(amount));
        transaction.setTime(Instant.now().toEpochMilli());
        transaction.setTransactionType(TRANSFER);
        transaction.setFailed(origin.getBalance() - amount < 0);

        origin.getTransactions().add(transaction);
        destination.getTransactions().add(new Transaction(transaction, amount));

        if (origin.getBalance() - amount < 0) {
            return false;
        }
        origin.setBalance(origin.getBalance() - amount);

        destination.setBalance(destination.getBalance() + amount);

        BankAccount updatedOrigin = this.bankAccountRepository.update(origin);
        BankAccount updatedDestination = this.bankAccountRepository.update(destination);

        return !(updatedOrigin == null || updatedDestination == null);
    }
}
