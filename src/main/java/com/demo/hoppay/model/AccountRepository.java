package com.demo.hoppay.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
	Optional<Account> findByAccountId(String accountId);
}
