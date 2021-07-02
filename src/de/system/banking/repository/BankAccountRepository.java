package de.system.banking.repository;

import java.util.List;
import java.util.Optional;

import de.system.banking.model.BankAccount;

public interface BankAccountRepository {

    Optional<BankAccount> findById(Long id) throws Exception;

    BankAccount insert(BankAccount bankAccount) throws Exception;

    BankAccount update(BankAccount bankAccount) throws Exception;

    List<BankAccount> findByCustomerId(Long customerId) throws Exception;
}
