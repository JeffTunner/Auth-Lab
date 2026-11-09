package com.auth.stateful_session_auth.service;

import com.auth.stateful_session_auth.dto.LoginRequest;
import com.auth.stateful_session_auth.dto.SignupRequest;
import com.auth.stateful_session_auth.entity.User;
import com.auth.stateful_session_auth.entity.UserSession;
import com.auth.stateful_session_auth.repository.UserRepository;
import com.auth.stateful_session_auth.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserSessionRepository userSessionRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public UserSession signup(SignupRequest r) {
        if(userRepository.existsByEmail(r.email()) || userRepository.existsByUsername(r.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email or username already exists!!!");
        }

        User user = new User();
        user.setName(r.name());
        user.setEmail(r.email());
        user.setUsername(r.username());
        user.setPasswordHash(passwordEncoder.encode(r.password()));
        userRepository.save(user);
        return createSession(user);
    }

    public UserSession login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(x -> passwordEncoder.matches(request.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return createSession(user);
    }

    public void logout(UUID sessionId) {
        userSessionRepository.deleteById(sessionId);
    }

    public UserSession createSession(User user) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setCreatedAt(Instant.now());
        session.setExpiresAt(Instant.now().plus(Duration.ofDays(7)));
        return userSessionRepository.save(session);
    }

}
