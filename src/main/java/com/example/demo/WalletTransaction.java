package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class WalletTransaction {
    @Id
    public String transactionId;
    public String fromAccount;
    public String toAccount;
    public BigDecimal amount;
    public String currency;
    public Instant createdAt;

    public WalletTransaction() {
    }

    public WalletTransaction(String transactionId, String fromAccount, String toAccount,
                             BigDecimal amount, String currency) {
        this.transactionId = transactionId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.createdAt = Instant.now();
    }
}