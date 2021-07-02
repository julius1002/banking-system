package de.system.banking.repository;

import java.util.*;
import java.util.stream.Collectors;

import de.system.banking.model.Customer;

public class InMemoryCustomerRepository implements CustomerRepository {

    private List<Customer> customers;

    public InMemoryCustomerRepository(List<Customer> customers) {
        this.customers = customers;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return this.customers.stream().filter(account -> account.getId().equals(id)).findFirst();
    }

    @Override
    public Customer update(Long id, Customer customer) {

        Optional<Customer> optionalCustomer = this.customers.stream().filter(account -> account.getId().equals(id))
                .findFirst();

        if (optionalCustomer.isEmpty()) {
            return null;
        }

        this.customers = this.customers.stream().filter(account -> !account.getId().equals(id))
                .collect(Collectors.toList());

        customer.setId(optionalCustomer.get().getId());
        customer.setBankAccounts(optionalCustomer.get().getBankAccounts());
        this.customers.add(customer);

        return customer;
    }

    @Override
    public Customer insert(Customer customer) {
        customer.setId(Math.abs(new Random().nextLong()));
        this.customers.add(customer);
        return customer;
    }


}
