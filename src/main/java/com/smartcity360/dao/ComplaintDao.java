package com.smartcity360.dao;

import com.smartcity360.chain.Complaint;
import com.smartcity360.persistence.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ComplaintDao {

    private static final Logger log = LoggerFactory.getLogger(ComplaintDao.class);

    public static class ComplaintRecord {
        private long id;
        private String title;
        private String residentName;
        private String description;
        private String location;
        private int severity;
        private String status;
        private String handledBy;
        private Timestamp createdAt;
        private List<String> auditTrail = new ArrayList<>();

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getResidentName() { return residentName; }
        public void setResidentName(String residentName) { this.residentName = residentName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public int getSeverity() { return severity; }
        public void setSeverity(int severity) { this.severity = severity; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getHandledBy() { return handledBy; }
        public void setHandledBy(String handledBy) { this.handledBy = handledBy; }
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
        public List<String> getAuditTrail() { return auditTrail; }
        public void setAuditTrail(List<String> auditTrail) { this.auditTrail = auditTrail; }
    }

    public long saveComplaint(Complaint complaint, String description, String location) {
        String insertComplaint = "INSERT INTO complaints (title, resident_name, description, location, severity, status, handled_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertTrail = "INSERT INTO complaint_trail (complaint_id, step_no, entry) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            long complaintId = -1;

            try (PreparedStatement ps = conn.prepareStatement(insertComplaint, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, complaint.getTitle());
                ps.setString(2, complaint.getResidentName());
                ps.setString(3, description != null ? description : "");
                ps.setString(4, location != null ? location : "Smart City Central");
                ps.setInt(5, complaint.getSeverityLevel());
                ps.setString(6, complaint.getStatus());
                ps.setString(7, complaint.getHandledBy());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        complaintId = rs.getLong(1);
                    }
                }
            }

            if (complaintId > 0 && !complaint.getAuditTrail().isEmpty()) {
                try (PreparedStatement psTrail = conn.prepareStatement(insertTrail)) {
                    int step = 1;
                    for (String entry : complaint.getAuditTrail()) {
                        psTrail.setLong(1, complaintId);
                        psTrail.setInt(2, step++);
                        psTrail.setString(3, entry);
                        psTrail.addBatch();
                    }
                    psTrail.executeBatch();
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            return complaintId;
        } catch (SQLException e) {
            log.error("Failed to persist complaint: {}", e.getMessage(), e);
            return -1;
        }
    }

    public List<ComplaintRecord> findAll() {
        List<ComplaintRecord> list = new ArrayList<>();
        String sql = "SELECT id, title, resident_name, description, location, severity, status, handled_by, created_at FROM complaints ORDER BY id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ComplaintRecord record = new ComplaintRecord();
                record.setId(rs.getLong("id"));
                record.setTitle(rs.getString("title"));
                record.setResidentName(rs.getString("resident_name"));
                record.setDescription(rs.getString("description"));
                record.setLocation(rs.getString("location"));
                record.setSeverity(rs.getInt("severity"));
                record.setStatus(rs.getString("status"));
                record.setHandledBy(rs.getString("handled_by"));
                record.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(record);
            }
        } catch (SQLException e) {
            log.error("Failed to load complaints: {}", e.getMessage(), e);
        }

        // Load trails
        for (ComplaintRecord rec : list) {
            rec.setAuditTrail(loadAuditTrail(rec.getId()));
        }

        return list;
    }

    public ComplaintRecord findById(long id) {
        String sql = "SELECT id, title, resident_name, description, location, severity, status, handled_by, created_at FROM complaints WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ComplaintRecord record = new ComplaintRecord();
                    record.setId(rs.getLong("id"));
                    record.setTitle(rs.getString("title"));
                    record.setResidentName(rs.getString("resident_name"));
                    record.setDescription(rs.getString("description"));
                    record.setLocation(rs.getString("location"));
                    record.setSeverity(rs.getInt("severity"));
                    record.setStatus(rs.getString("status"));
                    record.setHandledBy(rs.getString("handled_by"));
                    record.setCreatedAt(rs.getTimestamp("created_at"));
                    record.setAuditTrail(loadAuditTrail(id));
                    return record;
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find complaint #{}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    private List<String> loadAuditTrail(long complaintId) {
        List<String> trail = new ArrayList<>();
        String sql = "SELECT entry FROM complaint_trail WHERE complaint_id = ? ORDER BY step_no ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trail.add(rs.getString("entry"));
                }
            }
        } catch (SQLException e) {
            log.warn("Failed to load audit trail for complaint #{}: {}", complaintId, e.getMessage());
        }
        return trail;
    }

    public void seedInitialComplaintsIfEmpty() {
        String checkSql = "SELECT COUNT(*) FROM complaints";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                // Seed 3 realistic complaints showing all 3 handlers + fallback
                Complaint c1 = new Complaint("Broken street light", "John Doe", Complaint.SEVERITY_LOW);
                c1.addToAuditTrail("Local Staff: received the complaint");
                c1.addToAuditTrail("Local Staff: resolved the complaint");
                c1.markResolved("Local Staff");
                saveComplaint(c1, "Street light flickering near apartment building 3.", "Street 1-1");

                Complaint c2 = new Complaint("Water Pipe Leakage", "Sarah Smith", Complaint.SEVERITY_MEDIUM);
                c2.addToAuditTrail("Local Staff: received the complaint");
                c2.addToAuditTrail("Local Staff: cannot handle this severity, passing it to the next level");
                c2.addToAuditTrail("Supervisor: received the complaint");
                c2.addToAuditTrail("Supervisor: resolved the complaint");
                c2.markResolved("Supervisor");
                saveComplaint(c2, "High pressure water pipe leaking on main road.", "District 2 Central");

                Complaint c3 = new Complaint("District Power Grid Failure", "Mayor Office", Complaint.SEVERITY_HIGH);
                c3.addToAuditTrail("Local Staff: received the complaint");
                c3.addToAuditTrail("Local Staff: cannot handle this severity, passing it to the next level");
                c3.addToAuditTrail("Supervisor: received the complaint");
                c3.addToAuditTrail("Supervisor: cannot handle this severity, passing it to the next level");
                c3.addToAuditTrail("City Manager: received the complaint");
                c3.addToAuditTrail("City Manager: resolved the complaint");
                c3.markResolved("City Manager");
                saveComplaint(c3, "Total power black out affecting district 3 hospitals and shops.", "District 3");
            }
        } catch (SQLException e) {
            log.warn("Notice during initial complaints seed check: {}", e.getMessage());
        }
    }
}

