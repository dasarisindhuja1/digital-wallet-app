package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletEventRepository extends JpaRepository<WalletEvent, Long> {
	java.util.List<WalletEvent> findAllByOrderByIdAsc();
}