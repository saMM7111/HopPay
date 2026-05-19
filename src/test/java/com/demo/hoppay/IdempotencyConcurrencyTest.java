package com.demo.hoppay;

import com.demo.hoppay.model.Account;
import com.demo.hoppay.model.AccountRepository;
import com.demo.hoppay.model.PaymentInstruction;
import com.demo.hoppay.service.IdempotencyService;
import com.demo.hoppay.service.SettlementService;
import com.demo.hoppay.model.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class IdempotencyConcurrencyTest {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void singlePaymentSettlesOnce() throws Exception {
        accountRepository.save(new Account("alice@hoppay", new BigDecimal("1000"), "Alice"));
        accountRepository.save(new Account("bob@hoppay", new BigDecimal("200"), "Bob"));

        IdempotencyService idempotencyService = new IdempotencyService();
        SettlementService settlementService = new SettlementService(
                accountRepository,
                transactionRepository,
                idempotencyService
        );

        PaymentInstruction instruction = new PaymentInstruction(
                "tx-123",
                "alice@hoppay",
                "bob@hoppay",
                new BigDecimal("150"),
                System.currentTimeMillis()
        );

        int threads = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    settlementService.processPayment(instruction);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdownNow();

        Account sender = accountRepository.findByAccountId("alice@hoppay").orElseThrow();
        Account receiver = accountRepository.findByAccountId("bob@hoppay").orElseThrow();

        assertEquals(new BigDecimal("850"), sender.getBalance());
        assertEquals(new BigDecimal("350"), receiver.getBalance());
        assertEquals(1, transactionRepository.count());
    }
}
