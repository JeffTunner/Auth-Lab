package com.auth.stateful_session_auth.controller;

import com.auth.stateful_session_auth.dto.LoginRequest;
import com.auth.stateful_session_auth.dto.SignupRequest;
import com.auth.stateful_session_auth.entity.UserSession;
import com.auth.stateful_session_auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        return withCookie(authService.signup(request), "Signed Up");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        return withCookie(authService.login(request), "Logged in");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(name = "SESSION_ID", required = false) String id) {
        if(id != null) authService.logout(UUID.fromString(id));
        ResponseCookie clear = ResponseCookie.from("SESSION_ID", "")
                .httpOnly(true).path("/").maxAge(0).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, clear.toString()).body("Logged Out");
    }

    @GetMapping("/me")
    public String me(HttpServletRequest request) {
        return "Your user id: " +request.getAttribute("userId");
    }

    private ResponseEntity<String> withCookie(UserSession session, String msg) {
        ResponseCookie cookie = ResponseCookie.from("SESSION_ID", session.getId().toString())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(msg);
    }
}
