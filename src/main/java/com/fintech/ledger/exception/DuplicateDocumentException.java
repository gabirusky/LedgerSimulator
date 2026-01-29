package com.fintech.ledger.exception;

/**
 * Exception thrown when attempting to create an account with a document
 * that already exists in the system.
 */
public class DuplicateDocumentException extends RuntimeException {

    private final String document;

    public DuplicateDocumentException(String document) {
        super("An account with document '" + document + "' already exists");
        this.document = document;
    }

    public String getDocument() {
        return document;
    }
}
