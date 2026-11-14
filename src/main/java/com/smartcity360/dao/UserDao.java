package com.smartcity360.dao;

import com.smartcity360.persistence.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HexFormat;

/**
 * Data Access Object for system operator accounts and login authentication.
 */
public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    public static class UserRecord {
        private long id;
        private String username;
        private String fullName;
        private String role;
        private boolean active;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    /**
     * Computes a SHA-256 hex digest of the given password.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Authenticates a user by username and password.
     * Supports both SHA-256 hashed passwords and plain-text fallback for manual DB entries.
     */
    public UserRecord authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return null;
        }

        String sql = "SELECT id, username, password_hash, full_name, role, active FROM users WHERE username = ? AND active = TRUE";
        String hashedInput = hashPassword(password);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (storedHash != null && (storedHash.equalsIgnoreCase(hashedInput) || storedHash.equals(password))) {
                        UserRecord user = new UserRecord();
                        user.setId(rs.getLong("id"));
                        user.setUsername(rs.getString("username"));
                        user.setFullName(rs.getString("full_name"));
                        user.setRole(rs.getString("role"));
                        user.setActive(rs.getBoolean("active"));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Failed to authenticate user '{}': {}", username, e.getMessage(), e);
        }
        return null;
    }

    public boolean createUser(String username, String plainPassword, String fullName, String role) {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, active) VALUES (?, ?, ?, ?, TRUE)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, hashPassword(plainPassword));
            ps.setString(3, fullName.trim());
            ps.setString(4, role != null ? role.trim() : "OPERATOR");
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            log.warn("Could not create user '{}': {}", username, e.getMessage());
            return false;
        }
    }

    public void seedInitialUsersIfEmpty() {
        String checkSql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                log.info("Seeding initial SmartCity360 operator accounts...");
                createUser("admin", "admin123", "Municipal Administrator", "ADMIN");
                createUser("operator", "operator123", "City Grid Operator", "OPERATOR");
                log.info("Seeded default users: 'admin' and 'operator'.");
            }
        } catch (SQLException e) {
            log.warn("Notice during initial users seed check: {}", e.getMessage());
        }
    }
}

