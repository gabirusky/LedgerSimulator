package com.fintech.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Fintech Ledger Simulator.
 * 
 * This service implements a Double-Entry Bookkeeping system
 * with ACID compliance, pessimistic locking, and idempotency.
 */
@SpringBootApplication
public class LedgerSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedgerSimulatorApplication.class, args);
    }
}
