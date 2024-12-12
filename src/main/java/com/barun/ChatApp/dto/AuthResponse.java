package com.barun.ChatApp.dto;

public class AuthResponse {

    private String token;
    private String username;

    public AuthResponse() {}

    public AuthResponse(String username, String token) {
        this.username = username;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "username='" + username + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
