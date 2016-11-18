package com.myapp;

public class ApplicationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ApplicationException(String message) {
        super(message);
    }
}
