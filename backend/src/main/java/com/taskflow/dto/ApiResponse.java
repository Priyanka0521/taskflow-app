package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private T data;
    private String message;

    public ApiResponse() {
    }

    public ApiResponse(T data) {
        this.data = data;
    }

    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }

    public static <T> ApiResponse<T> message(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setMessage(message);
        return r;
    }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
