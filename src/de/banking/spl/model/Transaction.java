package de.banking.spl.model;

public class Transaction {

    private Long time;
    private Long amount;
    private TransactionType transactionType;
    private boolean failed;

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public Transaction() {
    }

    public Transaction(Transaction transaction, Long amount) {
        this.time = transaction.getTime();
        this.transactionType = transaction.getTransactionType();
        this.failed = transaction.isFailed();
        this.amount = amount;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
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
