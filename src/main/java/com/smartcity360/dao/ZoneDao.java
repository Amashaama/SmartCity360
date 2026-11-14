package com.smartcity360.dao;

import com.smartcity360.composite.Building;
import com.smartcity360.composite.CityZone;
import com.smartcity360.composite.CompositeZone;
import com.smartcity360.persistence.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ZoneDao {

    private static final Logger log = LoggerFactory.getLogger(ZoneDao.class);

    public CompositeZone loadCityHierarchy(String cityName) {
        String citySql = "SELECT id, name FROM zones WHERE parent_id IS NULL AND name = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(citySql)) {

            ps.setString(1, cityName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long cityId = rs.getLong("id");
                    CompositeZone city = new CompositeZone(rs.getString("name"));
                    city.setId(cityId);
                    loadChildren(conn, city, cityId);
                    return city;
                }
            }
        } catch (SQLException e) {
            log.error("Failed to load city hierarchy: {}", e.getMessage(), e);
        }
        return null;
    }

    private void loadChildren(Connection conn, CompositeZone parentZone, long parentId) throws SQLException {
        // 1. Load sub-zones (Districts or Streets)
        String subZoneSql = "SELECT id, name FROM zones WHERE parent_id = ? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(subZoneSql)) {
            ps.setLong(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long childId = rs.getLong("id");
                    String childName = rs.getString("name");
                    CompositeZone childZone = new CompositeZone(childName);
                    childZone.setId(childId);
                    loadChildren(conn, childZone, childId);
                    parentZone.addZone(childZone);
                }
            }
        }

        // 2. Load buildings under this zone (Streets hold buildings)
        String buildingSql = "SELECT id, name, power_usage, power_on FROM buildings WHERE street_id = ? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(buildingSql)) {
            ps.setLong(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Building building = new Building(rs.getString("name"), rs.getInt("power_usage"));
                    building.setId(rs.getLong("id"));
                    building.setPowerOn(rs.getBoolean("power_on"));
                    parentZone.addZone(building);
                }
            }
        }
    }

    public long saveZone(Long parentId, String name, String zoneType) throws SQLException {
        String sql = "INSERT INTO zones (parent_id, name, zone_type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (parentId == null || parentId <= 0) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, parentId);
            }
            ps.setString(2, name);
            ps.setString(3, zoneType);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1;
    }

    public long saveBuilding(long streetId, String name, int powerUsage, boolean powerOn) throws SQLException {
        String sql = "INSERT INTO buildings (street_id, name, power_usage, power_on) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, streetId);
            ps.setString(2, name);
            ps.setInt(3, powerUsage);
            ps.setBoolean(4, powerOn);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1;
    }

    public void updateBuilding(long id, String name, int powerUsage, boolean powerOn) throws SQLException {
        String sql = "UPDATE buildings SET name = ?, power_usage = ?, power_on = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, powerUsage);
            ps.setBoolean(3, powerOn);
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    public void updateBuildingPowerStatus(long id, boolean powerOn) throws SQLException {
        String sql = "UPDATE buildings SET power_on = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, powerOn);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void updateAllBuildingsPowerUnderZone(long zoneId, boolean powerOn) throws SQLException {
        // Recursive or hierarchy power update
        String updateDirectSql = "UPDATE buildings SET power_on = ? WHERE street_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateDirectSql)) {
            ps.setBoolean(1, powerOn);
            ps.setLong(2, zoneId);
            ps.executeUpdate();
        }

        // Also update nested children zones if zone is city/district
        String findChildrenSql = "SELECT id FROM zones WHERE parent_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(findChildrenSql)) {
            ps.setLong(1, zoneId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    updateAllBuildingsPowerUnderZone(rs.getLong("id"), powerOn);
                }
            }
        }
    }

    public void deleteZone(long id) throws SQLException {
        String sql = "DELETE FROM zones WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void deleteBuilding(long id) throws SQLException {
        String sql = "DELETE FROM buildings WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void seedInitialCityIfEmpty(String cityName) {
        String checkSql = "SELECT COUNT(*) FROM zones";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                log.info("Database zones empty. Seeding initial demo city...");
                long cityId = saveZone(null, cityName, "CITY");

                for (int d = 1; d <= 3; d++) {
                    long districtId = saveZone(cityId, "District " + d, "DISTRICT");
                    for (int s = 1; s <= 3; s++) {
                        long streetId = saveZone(districtId, "Street " + d + "-" + s, "STREET");
                        for (int b = 1; b <= 4; b++) {
                            int step = (d + s + b) % 8;
                            int usage = 100 + step * 10;
                            saveBuilding(streetId, "B" + d + "-" + s + "-" + b, usage, true);
                        }
                    }
                }
                log.info("Seeded initial demo city with 3 districts, 9 streets, 36 buildings.");
            }
        } catch (SQLException e) {
            log.warn("Notice during initial city seed check: {}", e.getMessage());
        }
    }
}

