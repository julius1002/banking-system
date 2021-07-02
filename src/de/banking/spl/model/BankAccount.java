package de.banking.spl.model;

import java.util.ArrayList;
import java.util.List;

public class BankAccount {

	private Long id;
	private Long balance;
	private List<Transaction> transactions = new ArrayList<Transaction>();
	private boolean overDraftEligible;
	private Long customer_id;

	public BankAccount() {
	}

	public BankAccount(Long id, Long balance) {
		this.id = id;
		this.balance = balance;
	}

	public BankAccount(Long balance, List<Transaction> transactions, boolean overDraftEligible, Long customer_id) {
		this.balance = balance;
		this.transactions = transactions;
		this.overDraftEligible = overDraftEligible;
		this.customer_id = customer_id;
	}

	public Long getCustomer_id() {
		return customer_id;
	}

	public void setCustomer_id(Long customer_id) {
		this.customer_id = customer_id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getBalance() {
		return balance;
	}

	public void setBalance(Long balance) {
		this.balance = balance;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public boolean isOverDraftEligible() {
		return overDraftEligible;
	}

	public void setOverDraftEligible(boolean overDraftEligible) {
		this.overDraftEligible = overDraftEligible;
	}
}
