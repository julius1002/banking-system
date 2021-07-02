package de.system.banking.model;

import java.util.List;

public class Customer {

    private Long id;
    private String firstName;
    private String lastName;
    private String passWord;
    private List<BankAccount> bankAccounts;

    public Customer() {
    }

    public Customer(Long id, String firstName, String lastName, String passWord, List<BankAccount> bankAccounts) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passWord = passWord;
        this.bankAccounts = bankAccounts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<BankAccount> getBankAccounts() {
        return bankAccounts;
    }

    public void setBankAccounts(List<BankAccount> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
