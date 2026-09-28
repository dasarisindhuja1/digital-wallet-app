package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletEventRepository eventRepository;

    public WalletService(WalletRepository walletRepository, WalletTransactionRepository transactionRepository,
                         WalletEventRepository eventRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Wallet createWallet(String accountId, BigDecimal openingBalance) {
        String normalizedId = requireText(accountId, "accountId");
        requireNonNegative(openingBalance, "openingBalance");
        if (walletRepository.existsById(normalizedId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account already exists");
        }
        String currency = "USD";
        Wallet wallet = walletRepository.save(new Wallet(normalizedId, openingBalance, currency));
        eventRepository.save(new WalletEvent("ACCOUNT_OPENED", normalizedId, openingBalance, currency, null));
        return wallet;
    }

    @Transactional
    public WalletTransaction transfer(TransferRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer request is required");
        }
        String transactionId = requireText(request.transactionId(), "transactionId");
        String fromId = requireText(request.fromAccount(), "fromAccount");
        String toId = requireText(request.toAccount(), "toAccount");
        requirePositive(request.amount(), "amount");
        String currency = requireText(request.currency(), "currency").toUpperCase();

        WalletTransaction existing = transactionRepository.findById(transactionId).orElse(null);
        if (existing != null) {
            if (!existing.fromAccount.equals(fromId)
                    || !existing.toAccount.equals(toId)
                    || existing.amount.compareTo(request.amount()) != 0
                    || !existing.currency.equalsIgnoreCase(currency)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "transactionId already belongs to a different transfer");
            }
            return existing;
        }
        if (fromId.equals(toId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Accounts must be different");
        }
        Wallet from = walletRepository.findById(fromId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));
        Wallet to = walletRepository.findById(toId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));
        if (from.balance.compareTo(request.amount()) < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient funds");
        }
        if (!currency.equalsIgnoreCase(from.currency) || !currency.equalsIgnoreCase(to.currency)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency does not match both accounts");
        }
        from.balance = from.balance.subtract(request.amount());
        to.balance = to.balance.add(request.amount());
        walletRepository.save(from);
        walletRepository.save(to);
        WalletTransaction transaction = transactionRepository.save(new WalletTransaction(transactionId, fromId, toId,
            request.amount(), currency));
        eventRepository.save(new WalletEvent("TRANSFER_DEBIT", fromId, request.amount().negate(), currency, transactionId));
        eventRepository.save(new WalletEvent("TRANSFER_CREDIT", toId, request.amount(), currency, transactionId));
        return transaction;
    }

    @Transactional(readOnly = true)
    public ReplayResult replay() {
        Map<String, BigDecimal> balances = new LinkedHashMap<>();
        List<WalletEvent> events = eventRepository.findAllByOrderByIdAsc();
        for (WalletEvent event : events) {
            balances.merge(event.accountId, event.amount, BigDecimal::add);
        }
        return new ReplayResult(events.size(), balances);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }

    private static void requirePositive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " must be greater than zero");
        }
    }

    private static void requireNonNegative(BigDecimal value, String field) {
        if (value == null || value.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " cannot be negative");
        }
    }

    public record TransferRequest(String fromAccount, String toAccount, BigDecimal amount,
                                  String currency, String transactionId) {
    }

    public record ReplayResult(int eventCount, Map<String, BigDecimal> balances) {
    }
}