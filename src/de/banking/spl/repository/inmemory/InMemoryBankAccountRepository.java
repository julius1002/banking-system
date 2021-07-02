package de.banking.spl.repository.inmemory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import de.banking.spl.model.BankAccount;
import de.banking.spl.repository.BankAccountRepository;

public class InMemoryBankAccountRepository implements BankAccountRepository {

	private List<BankAccount> bankAccounts;
	
	public InMemoryBankAccountRepository(List<BankAccount> bankAccounts) {
		this.bankAccounts = bankAccounts;
	}
	
	@Override
	public Optional<BankAccount> findById(Long id) {
		return this.bankAccounts.stream().filter(account -> account.getId().equals(id)).findFirst();
	}

	@Override
	public BankAccount insert(BankAccount bankAccount) {
		bankAccount.setId(Math.abs(new Random().nextLong()));
		bankAccount.setBalance(0L);
		this.bankAccounts.add(bankAccount);
		return bankAccount;
	}

	@Override
	public BankAccount update(BankAccount bankAccount) {
		BankAccount bankAccountToUpdate = null;
		for(BankAccount it: bankAccounts){
			if(it.getId().equals(bankAccount.getId())){
				bankAccountToUpdate = it;
			}
		}
		if(bankAccountToUpdate==null){
			return null;
		}

		bankAccounts.remove(bankAccountToUpdate);
		bankAccounts.add(bankAccount);
		return bankAccount;
	}

	@Override
	public List<BankAccount> findByCustomerId(Long customerId) {
		return Collections.emptyList();
	}
}
