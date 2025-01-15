package com.linkedinjobassistant.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Value("${aws.cognito.domain}")
    private String cognitoDomain;

    @Value("${aws.cognito.client-id}")
    private String clientId;

    @Value("${aws.cognito.logout-url}")
    private String logoutUrl;

    /**
     * Get current user info
     */
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OidcUser user) {
        if (user == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }

        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "user", Map.of(
                "id", user.getSubject(),
                "email", user.getEmail(),
                "name", user.getFullName(),
                "picture", user.getPicture()
            )
        ));
    }

    /**
     * Handle OAuth2 callback
     */
    @GetMapping("/callback")
    public ResponseEntity<?> handleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description) {
        
        if (error != null) {
            log.error("OAuth error: {} - {}", error, error_description);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", error,
                        "description", error_description
                    ));
        }

        if (code == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No authorization code provided"));
        }

        // The actual token exchange is handled by Spring Security OAuth2
        return ResponseEntity.ok(Map.of("message", "Authentication successful"));
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Construct Cognito logout URL
        String cognitoLogoutUrl = String.format("%s/logout?client_id=%s&logout_uri=%s",
                cognitoDomain,
                clientId,
                logoutUrl);

        return ResponseEntity.ok(Map.of(
            "message", "Logged out successfully",
            "logoutUrl", cognitoLogoutUrl
        ));
    }

    /**
     * Check authentication status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus(@AuthenticationPrincipal OidcUser user) {
        boolean isAuthenticated = user != null;
        
        Map<String, Object> status = Map.of(
            "authenticated", isAuthenticated,
            "session", Map.of(
                "active", isAuthenticated,
                "expiresAt", isAuthenticated ? 
                    user.getExpiresAt().toEpochMilli() : null
            )
        );

        return ResponseEntity.ok(status);
    }

    /**
     * Get login URL
     */
    @GetMapping("/login-url")
    public ResponseEntity<?> getLoginUrl() {
        String loginUrl = String.format("%s/login?client_id=%s&response_type=code",
                cognitoDomain,
                clientId);

        return ResponseEntity.ok(Map.of("loginUrl", loginUrl));
    }

    /**
     * Handle authentication errors
     */
    @GetMapping("/error")
    public ResponseEntity<?> handleAuthError(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description) {
        
        log.error("Authentication error: {} - {}", error, error_description);
        
        Map<String, Object> response = Map.of(
            "error", error != null ? error : "Unknown error",
            "description", error_description != null ? error_description : "An authentication error occurred",
            "timestamp", System.currentTimeMillis()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Refresh session
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshSession(@AuthenticationPrincipal OidcUser user) {
        if (user == null) {
            return ResponseEntity.ok(Map.of(
                "refreshed", false,
                "message", "No active session"
            ));
        }

        // The actual token refresh is handled by Spring Security OAuth2
        return ResponseEntity.ok(Map.of(
            "refreshed", true,
            "expiresAt", user.getExpiresAt().toEpochMilli()
        ));
    }
}
