package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class WalletEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String eventType;
    public String accountId;
    public BigDecimal amount;
    public String currency;
    public String transactionId;
    public Instant createdAt;

    public WalletEvent() {
    }

    public WalletEvent(String eventType, String accountId, BigDecimal amount,
                       String currency, String transactionId) {
        this.eventType = eventType;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.transactionId = transactionId;
        this.createdAt = Instant.now();
    }
}