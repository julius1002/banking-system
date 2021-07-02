package de.system.banking.service;

import java.time.Instant;
import java.util.Optional;

import de.system.banking.model.BankAccount;
import de.system.banking.model.BankingTransaction;
import de.system.banking.model.BankingTransactionType;
import de.system.banking.repository.BankAccountRepository;

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

        var transaction = new BankingTransaction();
        transaction.setAmount(amount);
        transaction.setTime(Instant.now().toEpochMilli());
        transaction.setTransactionType(BankingTransactionType.WITHDRAW);
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

        var transaction = new BankingTransaction();

        transaction.setAmount(amount);
        transaction.setTime(Instant.now().toEpochMilli());
        transaction.setTransactionType(BankingTransactionType.DEPOSIT);
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

        BankingTransaction bankingTransaction = new BankingTransaction();
        bankingTransaction.setAmount(Math.negateExact(amount));
        bankingTransaction.setTime(Instant.now().toEpochMilli());
        bankingTransaction.setTransactionType(BankingTransactionType.TRANSFER);
        bankingTransaction.setFailed(origin.getBalance() - amount < 0);

        origin.getTransactions().add(bankingTransaction);
        destination.getTransactions().add(new BankingTransaction(bankingTransaction, amount));

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
