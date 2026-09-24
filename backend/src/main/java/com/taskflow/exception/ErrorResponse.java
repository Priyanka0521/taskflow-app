package com.taskflow.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {

    private String message;
    private Map<String, String> errors;
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message) {
        this();
        this.message = message;
    }

    public ErrorResponse(String message, Map<String, String> errors) {
        this();
        this.message = message;
        this.errors = errors;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Map<String, String> getErrors() { return errors; }
    public void setErrors(Map<String, String> errors) { this.errors = errors; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
