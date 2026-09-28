package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class Wallet {
    
    @Id
    public String accountId;
    public BigDecimal balance;
    public String currency;

    // An empty constructor is required by the database
    public Wallet() {
    }

    // A constructor we can use to easily create new wallets
    public Wallet(String accountId, BigDecimal balance) {
        this(accountId, balance, "USD");
    }

    public Wallet(String accountId, BigDecimal balance, String currency) {
        this.accountId = accountId;
        this.balance = balance;
        this.currency = currency;
    }
}
