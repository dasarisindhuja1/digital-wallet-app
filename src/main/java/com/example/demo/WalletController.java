package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/wallet")
public class WalletController {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletService walletService;

    public WalletController(WalletRepository walletRepository, WalletTransactionRepository transactionRepository,
                            WalletService walletService) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
    }

    @GetMapping("/wallets")
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    @GetMapping("/wallets/{accountId}")
    public Wallet getWallet(@PathVariable String accountId) {
        return walletRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    @PostMapping("/wallets")
    public Wallet createWallet(@RequestBody CreateWalletRequest request) {
        return walletService.createWallet(request.accountId(), request.openingBalance());
    }

    @PostMapping("/balance_transfer")
    public TransferResponse transfer(@RequestBody WalletService.TransferRequest request) {
        WalletTransaction transaction = walletService.transfer(request);
        return new TransferResponse("success", transaction.transactionId);
    }

    @GetMapping("/transactions")
    public List<WalletTransaction> getTransactions() {
        return transactionRepository.findAll();
    }

    @GetMapping("/replay")
    public WalletService.ReplayResult replay() {
        return walletService.replay();
    }

    @PostMapping("/legacy/create")
    public Wallet legacyCreate(@RequestParam String id, @RequestParam BigDecimal balance) {
        return walletService.createWallet(id, balance);
    }

    public record CreateWalletRequest(String accountId, BigDecimal openingBalance) {
    }

    public record TransferResponse(String status, String transactionId) {
    }
}