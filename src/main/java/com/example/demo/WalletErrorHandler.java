package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class WalletErrorHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<WalletError> handleWalletError(ResponseStatusException exception) {
        String reason = exception.getReason() == null ? exception.getStatusCode().toString() : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode())
                .body(new WalletError("WALLET_REQUEST_FAILED", reason));
    }

    public record WalletError(String error, String message) {
    }
}