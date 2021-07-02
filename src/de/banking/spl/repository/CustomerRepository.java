package de.banking.spl.repository;

import java.util.Optional;

import de.banking.spl.model.Customer;

public interface CustomerRepository {

	Optional<Customer> findById(Long id) throws Exception;

	Customer update(Long id, Customer customer) throws Exception;

	Customer insert(Customer customer) throws Exception;
}
