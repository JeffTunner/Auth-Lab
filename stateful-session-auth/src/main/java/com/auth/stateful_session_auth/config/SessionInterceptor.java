package com.auth.stateful_session_auth.config;

import com.auth.stateful_session_auth.entity.UserSession;
import com.auth.stateful_session_auth.repository.UserSessionRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Autowired
    UserSessionRepository sessionRepository;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
        if(req.getCookies() != null) {
            for(Cookie c: req.getCookies()) {
                if(!c.getName().equals("SESSION_ID")) continue;
                try {
                    UUID sid = UUID.fromString(c.getValue());
                    Optional<UserSession> s = sessionRepository.findByIdAndExpiresAtAfter(sid, Instant.now());
                    if(s.isPresent()) {
                        req.setAttribute("userId", s.get().getUser().getId());
                        return true;
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not Logged in");
        return false;
    }
}
