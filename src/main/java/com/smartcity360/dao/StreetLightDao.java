package com.smartcity360.dao;

import com.smartcity360.flyweight.StreetLight;
import com.smartcity360.flyweight.StreetLightFactory;
import com.smartcity360.flyweight.StreetLightType;
import com.smartcity360.persistence.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class StreetLightDao {

    private static final Logger log = LoggerFactory.getLogger(StreetLightDao.class);

    public static class LightRecord {
        private long id;
        private String lightCode;
        private String gps;
        private boolean on;
        private long typeId;
        private String typeName;
        private String bulbModel;
        private int wattage;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getLightCode() { return lightCode; }
        public void setLightCode(String lightCode) { this.lightCode = lightCode; }
        public String getGps() { return gps; }
        public void setGps(String gps) { this.gps = gps; }
        public boolean isOn() { return on; }
        public void setOn(boolean on) { this.on = on; }
        public long getTypeId() { return typeId; }
        public void setTypeId(long typeId) { this.typeId = typeId; }
        public String getTypeName() { return typeName; }
        public void setTypeName(String typeName) { this.typeName = typeName; }
        public String getBulbModel() { return bulbModel; }
        public void setBulbModel(String bulbModel) { this.bulbModel = bulbModel; }
        public int getWattage() { return wattage; }
        public void setWattage(int wattage) { this.wattage = wattage; }
    }

    public long getOrCreateTypeId(String typeName, String bulbModel, int wattage) {
        String findSql = "SELECT id FROM light_types WHERE type_name = ? AND bulb_model = ? AND wattage = ?";
        String insertSql = "INSERT INTO light_types (type_name, bulb_model, wattage) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setString(1, typeName);
                ps.setString(2, bulbModel);
                ps.setInt(3, wattage);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("id");
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, typeName);
                ps.setString(2, bulbModel);
                ps.setInt(3, wattage);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Failed getOrCreateTypeId: {}", e.getMessage(), e);
        }
        return -1;
    }

    public boolean addStreetLight(String lightCode, String gps, boolean isOn, String typeName, String bulbModel, int wattage) {
        long typeId = getOrCreateTypeId(typeName, bulbModel, wattage);
        if (typeId <= 0) return false;

        String insertSql = "INSERT INTO street_lights (light_code, gps, is_on, type_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            ps.setString(1, lightCode);
            ps.setString(2, gps);
            ps.setBoolean(3, isOn);
            ps.setLong(4, typeId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            log.error("Failed to add street light: {}", e.getMessage(), e);
            return false;
        }
    }

    public void toggleLight(long id, boolean newState) {
        String sql = "UPDATE street_lights SET is_on = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, newState);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to toggle light #{}: {}", id, e.getMessage(), e);
        }
    }

    public List<LightRecord> findRecentLights(int limit) {
        List<LightRecord> list = new ArrayList<>();
        String sql = "SELECT l.id, l.light_code, l.gps, l.is_on, t.id AS type_id, t.type_name, t.bulb_model, t.wattage " +
                "FROM street_lights l JOIN light_types t ON l.type_id = t.id ORDER BY l.id DESC LIMIT ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LightRecord r = new LightRecord();
                    r.setId(rs.getLong("id"));
                    r.setLightCode(rs.getString("light_code"));
                    r.setGps(rs.getString("gps"));
                    r.setOn(rs.getBoolean("is_on"));
                    r.setTypeId(rs.getLong("type_id"));
                    r.setTypeName(rs.getString("type_name"));
                    r.setBulbModel(rs.getString("bulb_model"));
                    r.setWattage(rs.getInt("wattage"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find street lights: {}", e.getMessage(), e);
        }
        return list;
    }

    public int countTotalLights() {
        String sql = "SELECT COUNT(*) FROM street_lights";
        try (Connection conn = DatabaseManager.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("Count lights failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    public int countDistinctTypes() {
        String sql = "SELECT COUNT(*) FROM light_types";
        try (Connection conn = DatabaseManager.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("Count types failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    public void seedInitialLightsIfEmpty() {
        String checkSql = "SELECT COUNT(*) FROM street_lights";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                log.info("Seeding initial street lights into DB...");
                String[] typeNames = {"LED-Standard", "LED-Bright", "Sodium-Classic", "Solar-Eco", "Halogen-Old"};
                String[] bulbModels = {"Philips-X200", "Osram-B300", "GE-S100", "Sun-E50", "Lumi-H75"};
                int[] wattages = {45, 60, 70, 30, 90};

                for (int i = 0; i < 50; i++) {
                    int tIdx = i % typeNames.length;
                    double lat = 40.7000 + (i / 10) * 0.001;
                    double lon = -74.0200 + (i % 10) * 0.001;
                    String gps = String.format(java.util.Locale.US, "%.4f, %.4f", lat, lon);
                    addStreetLight("SL-" + (i + 1), gps, i % 2 == 0, typeNames[tIdx], bulbModels[tIdx], wattages[tIdx]);
                }
                log.info("Seeded 50 initial street lights across 5 shared types.");
            }
        } catch (SQLException e) {
            log.warn("Notice during initial street light seed: {}", e.getMessage());
        }
    }
}

