package com.severett.paymentprocessor.exceptions;

public class InvalidUploadException extends Exception {
    
    public InvalidUploadException(String reason) {
        super(reason);
    }
    
}
