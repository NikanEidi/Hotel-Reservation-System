package com.seneca.hotelreservation_system.service;

import com.seneca.hotelreservation_system.model.AdminRole;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    public static class AdminSession {
        private final String username;
        private final AdminRole role;

        public AdminSession(String username, AdminRole role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public AdminRole getRole() {
            return role;
        }
    }

    private static final Map<String, String> PASSWORD_HASHES = new HashMap<>();
    private static final Map<String, AdminRole> USER_ROLES = new HashMap<>();
    private static AdminSession currentSession;

    static {
        PASSWORD_HASHES.put("admin", BCrypt.hashpw("admin123", BCrypt.gensalt()));
        PASSWORD_HASHES.put("manager", BCrypt.hashpw("manager123", BCrypt.gensalt()));

        USER_ROLES.put("admin", AdminRole.ADMIN);
        USER_ROLES.put("manager", AdminRole.MANAGER);
    }

    public AdminSession login(String username, String password) {
        String normalized = username == null ? "" : username.trim().toLowerCase();
        String rawPassword = password == null ? "" : password.trim();

        if (!PASSWORD_HASHES.containsKey(normalized)) {
            return null;
        }

        boolean valid = BCrypt.checkpw(rawPassword, PASSWORD_HASHES.get(normalized));
        if (!valid) {
            return null;
        }

        currentSession = new AdminSession(normalized, USER_ROLES.get(normalized));
        return currentSession;
    }

    public void logout() {
        currentSession = null;
    }

    public AdminSession getCurrentSession() {
        return currentSession;
    }

    public boolean isLoggedIn() {
        return currentSession != null;
    }
}