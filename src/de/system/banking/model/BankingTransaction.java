package de.system.banking.model;

public class BankingTransaction {

    private Long time;
    private Long amount;
    private BankingTransactionType bankingTransactionType;
    private boolean failed;

    public BankingTransactionType getTransactionType() {
        return bankingTransactionType;
    }

    public BankingTransaction() {
    }

    public BankingTransaction(BankingTransaction bankingTransaction, Long amount) {
        this.time = bankingTransaction.getTime();
        this.bankingTransactionType = bankingTransaction.getTransactionType();
        this.failed = bankingTransaction.isFailed();
        this.amount = amount;
    }

    public void setTransactionType(BankingTransactionType bankingTransactionType) {
        this.bankingTransactionType = bankingTransactionType;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }
}
