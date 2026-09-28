package com.example.demo;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends JpaRepository<Wallet, String> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select wallet from Wallet wallet where wallet.accountId = :accountId")
	java.util.Optional<Wallet> findByIdForUpdate(@Param("accountId") String accountId);
}