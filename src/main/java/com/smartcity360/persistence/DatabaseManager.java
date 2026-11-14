package com.smartcity360.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Manages JDBC connection pooling with HikariCP.
 
 * If MySQL is unreachable, it automatically falls back to an embedded in-memory H2 database

 */
public final class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private static HikariDataSource dataSource;
    private static boolean isFallbackInMemory = false;
    private static String activeDatabaseType = "Unknown";
    private static String statusMessage = "";

    private DatabaseManager() {}

    public static synchronized void initialize() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        Properties props = loadProperties();

        String url = System.getenv("DB_URL");
        if (url == null || url.isBlank()) {
            url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/smartcity360?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        }

        String user = System.getenv("DB_USER");
        if (user == null || user.isBlank()) {
            user = props.getProperty("db.user", "root");
        }

        String password = System.getenv("DB_PASSWORD");
        if (password == null) {
            password = props.getProperty("db.password", "");
        }


        try {
            log.info("Attempting connection to primary MySQL database at {}", url);
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(3000); // 3 seconds timeout
            config.setInitializationFailTimeout(3000);
            config.setPoolName("SmartCity360-MySQL-Pool");

            dataSource = new HikariDataSource(config);
          
            try (Connection conn = dataSource.getConnection()) {
                activeDatabaseType = "MySQL 8 (" + conn.getMetaData().getDatabaseProductVersion() + ")";
                isFallbackInMemory = false;
                statusMessage = "Connected to MySQL successfully.";
                log.info("Successfully connected to MySQL database: {}", activeDatabaseType);
            }
        } catch (Exception ex) {
            log.warn("MySQL connection failed: {}. Falling back to in-memory H2 database.", ex.getMessage());
            initH2Fallback();
        }

        
        executeSchemaScript();
    }

    private static void initH2Fallback() {
        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.h2.Driver");
            config.setJdbcUrl("jdbc:h2:mem:smartcity360;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
            config.setUsername("sa");
            config.setPassword("");
            config.setMaximumPoolSize(10);
            config.setPoolName("SmartCity360-H2-Pool");

            dataSource = new HikariDataSource(config);
            activeDatabaseType = "H2 In-Memory (MySQL compatibility mode)";
            isFallbackInMemory = true;
            statusMessage = "Running in In-Memory fallback mode. (MySQL server was offline or unreachable).";
            log.info("Fallback H2 database initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize even fallback in-memory database!", e);
            throw new RuntimeException("Cannot initialize any database", e);
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            log.warn("Could not load db.properties: {}", e.getMessage());
        }
        return props;
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        return dataSource.getConnection();
    }

    private static void executeSchemaScript() {
        try (InputStream in = DatabaseManager.class.getClassLoader().getResourceAsStream("db/schema.sql")) {
            if (in == null) {
                log.warn("schema.sql not found on classpath!");
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                 Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("--") || line.isBlank()) {
                        continue;
                    }
                    sql.append(line).append(" ");
                    if (line.endsWith(";")) {
                        String statementStr = sql.toString().replace(";", "").trim();
                        if (!statementStr.isEmpty()) {
                            try {
                                stmt.execute(statementStr);
                            } catch (SQLException e) {
                                log.debug("Notice on statement [{}]: {}", statementStr, e.getMessage());
                            }
                        }
                        sql.setLength(0);
                    }
                }
                log.info("Database schema applied successfully.");
            }
        } catch (Exception e) {
            log.error("Error executing schema script: {}", e.getMessage(), e);
        }
    }

    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("Database pool closed.");
        }
    }

    public static boolean isFallbackInMemory() {
        return isFallbackInMemory;
    }

    public static String getActiveDatabaseType() {
        return activeDatabaseType;
    }

    public static String getStatusMessage() {
        return statusMessage;
    }
}

