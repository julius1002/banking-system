package de.banking.spl.repository;

import java.util.List;
import java.util.Optional;

import de.banking.spl.model.BankAccount;

public interface BankAccountRepository {

    Optional<BankAccount> findById(Long id) throws Exception;

    BankAccount insert(BankAccount bankAccount) throws Exception;

    BankAccount update(BankAccount bankAccount) throws Exception;

    List<BankAccount> findByCustomerId(Long customerId) throws Exception;
}
