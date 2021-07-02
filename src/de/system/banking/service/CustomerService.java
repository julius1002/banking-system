package de.system.banking.service;

import de.system.banking.model.Customer;
import de.system.banking.repository.CustomerRepository;

public class CustomerService {

    private CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    public Customer changeUserDetails(Long customerId, Customer update) throws Exception {

        var optionalCustomer = customerRepository.findById(customerId);

        var customer = optionalCustomer.isEmpty() ? null : optionalCustomer.get();

        if (customer == null) {
            return null;
        }

        update.setPassWord(optionalCustomer.get().getPassWord());

        return customerRepository.update(customerId, update);
    }

    public Customer insertCustomer(Customer customer) throws Exception {
        return customerRepository.insert(customer);
    }

}
