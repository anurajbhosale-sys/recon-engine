package com.recon.ledger;

// The same idempotency key was sent with a DIFFERENT request → client error (422)
public class IdempotencyKeyReusedException extends RuntimeException {
    public IdempotencyKeyReusedException(String idempotencyKey) {
        super("Idempotency key '" + idempotencyKey + "' was already used for a different request");
    }
}