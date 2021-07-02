package de.system.banking.repository;

import java.util.Optional;

import de.system.banking.model.Customer;

public interface CustomerRepository {

	Optional<Customer> findById(Long id) throws Exception;

	Customer update(Long id, Customer customer) throws Exception;

	Customer insert(Customer customer) throws Exception;
}
