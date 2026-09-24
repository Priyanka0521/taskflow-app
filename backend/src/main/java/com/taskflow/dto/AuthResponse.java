package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {

    private String token;
    private UserResponse user;

    public AuthResponse() {
    }

    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    @JsonProperty("token")
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    @JsonProperty("user")
    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }
}
