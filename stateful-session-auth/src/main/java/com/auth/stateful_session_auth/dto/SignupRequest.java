package com.auth.stateful_session_auth.dto;

public record SignupRequest(String name, String email, String username, String password) {
}
