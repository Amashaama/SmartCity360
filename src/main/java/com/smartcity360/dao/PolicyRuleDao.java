package com.smartcity360.dao;

import com.smartcity360.persistence.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PolicyRuleDao {

    private static final Logger log = LoggerFactory.getLogger(PolicyRuleDao.class);

    public static class RuleEntity {
        private long id;
        private String ruleName;
        private String ruleText;
        private String description;
        private boolean active;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getRuleText() { return ruleText; }
        public void setRuleText(String ruleText) { this.ruleText = ruleText; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    public List<RuleEntity> findAll() {
        List<RuleEntity> list = new ArrayList<>();
        String sql = "SELECT id, rule_name, rule_text, description, active FROM policy_rules ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RuleEntity r = new RuleEntity();
                r.setId(rs.getLong("id"));
                r.setRuleName(rs.getString("rule_name"));
                r.setRuleText(rs.getString("rule_text"));
                r.setDescription(rs.getString("description"));
                r.setActive(rs.getBoolean("active"));
                list.add(r);
            }
        } catch (SQLException e) {
            log.error("Failed to load policy rules: {}", e.getMessage(), e);
        }
        return list;
    }

    public boolean saveRule(String ruleName, String ruleText, String description) {
        String sql = "INSERT INTO policy_rules (rule_name, rule_text, description, active) VALUES (?, ?, ?, true)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruleName);
            ps.setString(2, ruleText);
            ps.setString(3, description);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            log.error("Failed to save policy rule: {}", e.getMessage(), e);
            return false;
        }
    }

    public void deleteRule(long id) {
        String sql = "DELETE FROM policy_rules WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete policy rule #{}: {}", id, e.getMessage(), e);
        }
    }

    public void toggleActive(long id, boolean active) {
        String sql = "UPDATE policy_rules SET active = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to toggle rule active #{}: {}", id, e.getMessage(), e);
        }
    }

    public Set<String> getAllKeywords() {
        Set<String> set = new HashSet<>();
        String sql = "SELECT keyword FROM policy_keywords ORDER BY keyword";
        try (Connection conn = DatabaseManager.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                set.add(rs.getString("keyword"));
            }
        } catch (SQLException e) {
            log.error("Failed to load keywords: {}", e.getMessage(), e);
        }
        return set;
    }

    public void seedInitialRulesIfEmpty() {
        String checkSql = "SELECT COUNT(*) FROM policy_rules";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                log.info("Seeding initial policy keywords & rules...");
                // Seed keywords
                String[] kws = {
                        "HIGH_NOISE", "Decibel levels exceeding urban quiet zone threshold",
                        "RESTRICTED_HOURS", "Designated quiet hours (10:00 PM - 06:00 AM)",
                        "ILLEGAL_PARKING", "Vehicle parked in non-designated or fire hydrant zone",
                        "HAZARDOUS_SMOKE", "Air quality sensor detects toxic gas or open burn",
                        "EMERGENCY_ZONE", "Designated hospital or civil defense access corridor"
                };
                for (int i = 0; i < kws.length; i += 2) {
                    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO policy_keywords (keyword, description) VALUES (?, ?)")) {
                        ps.setString(1, kws[i]);
                        ps.setString(2, kws[i + 1]);
                        ps.executeUpdate();
                    } catch (SQLException ignored) {}
                }

                saveRule("Noise Violation Rule", "HIGH_NOISE AND RESTRICTED_HOURS",
                        "Triggers penalty if high decibel noise is recorded during nighttime restricted hours.");
                saveRule("Public Safety Violation Rule", "(HIGH_NOISE OR ILLEGAL_PARKING) AND RESTRICTED_HOURS",
                        "Composite rule combining noise and illegal parking during night hours.");
                saveRule("Critical Emergency Rule", "EMERGENCY_ZONE AND (HAZARDOUS_SMOKE OR ILLEGAL_PARKING)",
                        "Strict rule protecting critical emergency corridors from blockages or fire threats.");
                log.info("Seeded initial policy rules and DSL keywords.");
            }
        } catch (SQLException e) {
            log.warn("Notice during policy rule seed check: {}", e.getMessage());
        }
    }
}

