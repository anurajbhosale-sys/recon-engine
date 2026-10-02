package com.recon.shared.web;

import com.recon.ledger.AccountNotFoundException;
import com.recon.ledger.EntryNotFoundException;
import com.recon.ledger.EntryNotReversibleException;
import com.recon.ledger.InvalidJournalEntryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// CONCEPT: one place that turns exceptions from ANY controller into clean HTTP responses.
// Extending ResponseEntityExceptionHandler gives us Spring's built-in handling for
// validation errors, missing headers, and broken JSON, all as 400 ProblemDetails.
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // The client broke a ledger rule → their fault → 400 with a helpful message
    @ExceptionHandler(InvalidJournalEntryException.class)
    public ProblemDetail handleInvalidEntry(InvalidJournalEntryException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Invalid journal entry");
        return problem;
    }

    // Anything we didn't expect → a bug on OUR side → 500
    // CONCEPT: log the full details for us; tell the client nothing about our internals
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal error");
        return problem;
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFound(AccountNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Account not found");
        return problem;
    }

    @ExceptionHandler(EntryNotFoundException.class)
    public ProblemDetail handleEntryNotFound(EntryNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Journal entry not found");
        return problem;
    }

    // CONCEPT: 409 Conflict = the request is valid, but clashes with the current state
    @ExceptionHandler(EntryNotReversibleException.class)
    public ProblemDetail handleNotReversible(EntryNotReversibleException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Entry cannot be reversed");
        return problem;
    }
}