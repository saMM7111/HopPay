package com.demo.hoppay.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {
	private final ConcurrentHashMap<String, Instant> seenTxIds = new ConcurrentHashMap<>();

	public boolean claim(String txId) {
		Instant now = Instant.now();
		Instant existing = seenTxIds.putIfAbsent(txId, now);
		return existing == null;
	}
}
