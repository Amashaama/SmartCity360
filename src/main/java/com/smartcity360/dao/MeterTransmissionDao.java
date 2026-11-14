package com.smartcity360.dao;

import com.smartcity360.persistence.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class MeterTransmissionDao {

    private static final Logger log = LoggerFactory.getLogger(MeterTransmissionDao.class);

    public static class TransmissionRecord {
        private long id;
        private String meterId;
        private String originalReading;
        private boolean useCompression;
        private boolean useEncryption;
        private boolean useAuth;
        private String finalSent;
        private String readBack;
        private Timestamp createdAt;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getMeterId() { return meterId; }
        public void setMeterId(String meterId) { this.meterId = meterId; }
        public String getOriginalReading() { return originalReading; }
        public void setOriginalReading(String originalReading) { this.originalReading = originalReading; }
        public boolean isUseCompression() { return useCompression; }
        public void setUseCompression(boolean useCompression) { this.useCompression = useCompression; }
        public boolean isUseEncryption() { return useEncryption; }
        public void setUseEncryption(boolean useEncryption) { this.useEncryption = useEncryption; }
        public boolean isUseAuth() { return useAuth; }
        public void setUseAuth(boolean useAuth) { this.useAuth = useAuth; }
        public String getFinalSent() { return finalSent; }
        public void setFinalSent(String finalSent) { this.finalSent = finalSent; }
        public String getReadBack() { return readBack; }
        public void setReadBack(String readBack) { this.readBack = readBack; }
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    }

    public void logTransmission(String meterId, String original, boolean compress, boolean encrypt, boolean auth, String finalSent, String readBack) {
        String sql = "INSERT INTO meter_transmissions (meter_id, original_reading, use_compression, use_encryption, use_auth, final_sent, read_back) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, meterId);
            ps.setString(2, original);
            ps.setBoolean(3, compress);
            ps.setBoolean(4, encrypt);
            ps.setBoolean(5, auth);
            ps.setString(6, finalSent);
            ps.setString(7, readBack);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to log transmission: {}", e.getMessage(), e);
        }
    }

    public List<TransmissionRecord> findRecent(int limit) {
        List<TransmissionRecord> list = new ArrayList<>();
        String sql = "SELECT id, meter_id, original_reading, use_compression, use_encryption, use_auth, final_sent, read_back, created_at " +
                "FROM meter_transmissions ORDER BY id DESC LIMIT ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TransmissionRecord r = new TransmissionRecord();
                    r.setId(rs.getLong("id"));
                    r.setMeterId(rs.getString("meter_id"));
                    r.setOriginalReading(rs.getString("original_reading"));
                    r.setUseCompression(rs.getBoolean("use_compression"));
                    r.setUseEncryption(rs.getBoolean("use_encryption"));
                    r.setUseAuth(rs.getBoolean("use_auth"));
                    r.setFinalSent(rs.getString("final_sent"));
                    r.setReadBack(rs.getString("read_back"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find recent transmissions: {}", e.getMessage(), e);
        }
        return list;
    }

    public void seedInitialTransmissionsIfEmpty() {
        String checkSql = "SELECT COUNT(*) FROM meter_transmissions";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                log.info("Seeding initial meter telemetry transmissions...");
                String tok1 = com.smartcity360.decorator.AuthenticationDecorator.generateDeviceToken("METER-101");
                String raw1 = "Meter 101:   420.5 kWh   at 08:00 [Normal Load]";
                String comp1 = com.smartcity360.decorator.CompressionDecorator.compress(raw1);
                String enc1 = com.smartcity360.decorator.EncryptionDecorator.encrypt(comp1);
                logTransmission("METER-101", raw1, true, true, true,
                        "TOKEN:" + tok1 + "|" + enc1, comp1);

                String tok2 = com.smartcity360.decorator.AuthenticationDecorator.generateDeviceToken("METER-204");
                String raw2 = "Meter 204:   185.0 kWh   at 09:15 [Peak Load]";
                String comp2 = com.smartcity360.decorator.CompressionDecorator.compress(raw2);
                String enc2 = com.smartcity360.decorator.EncryptionDecorator.encrypt(comp2);
                logTransmission("METER-204", raw2, true, true, true,
                        "TOKEN:" + tok2 + "|" + enc2, comp2);
            }
        } catch (SQLException e) {
            log.warn("Notice during initial meter transmission seed: {}", e.getMessage());
        }
    }
}

