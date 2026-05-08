package com.demo.hoppay.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	@Query("select coalesce(sum(t.amount), 0) from Transaction t")
	BigDecimal totalVolume();
}
