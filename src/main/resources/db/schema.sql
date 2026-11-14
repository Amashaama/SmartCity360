-- SmartCity360 Database Schema
-- UrbanTech Solutions - Municipal Infrastructure Management Platform

-- Municipal Zones: Administrative hierarchy (City -> District -> Street)
CREATE TABLE IF NOT EXISTS zones (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT NULL,
    name        VARCHAR(100) NOT NULL,
    zone_type   VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_zone_parent FOREIGN KEY (parent_id) REFERENCES zones (id) ON DELETE CASCADE,
    CONSTRAINT uq_zone_name UNIQUE (parent_id, name)
);

-- Buildings: Individual facilities and meter termination points under streets
CREATE TABLE IF NOT EXISTS buildings (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    street_id    BIGINT NOT NULL,
    name         VARCHAR(100) NOT NULL,
    power_usage  INT NOT NULL,
    power_on     BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_building_street FOREIGN KEY (street_id) REFERENCES zones (id) ON DELETE CASCADE,
    CONSTRAINT uq_building_name UNIQUE (street_id, name)
);

-- Citizen Complaints: Grievance intake and automated escalation tracking
CREATE TABLE IF NOT EXISTS complaints (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(150) NOT NULL,
    resident_name  VARCHAR(100) NOT NULL,
    description    VARCHAR(1000),
    location       VARCHAR(150),
    severity       INT NOT NULL,
    status         VARCHAR(40) NOT NULL,
    handled_by     VARCHAR(60) NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS complaint_trail (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id  BIGINT NOT NULL,
    step_no       INT NOT NULL,
    entry         VARCHAR(255) NOT NULL,
    CONSTRAINT fk_trail_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id) ON DELETE CASCADE
);

-- Lighting Hardware Specifications: Shared bulb profiles and power ratings
CREATE TABLE IF NOT EXISTS light_types (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_name   VARCHAR(60) NOT NULL,
    bulb_model  VARCHAR(60) NOT NULL,
    wattage     INT NOT NULL,
    CONSTRAINT uq_light_type UNIQUE (type_name, bulb_model, wattage)
);

-- Street Lights: Physical streetlight deployments mapped to geographical coordinates
CREATE TABLE IF NOT EXISTS street_lights (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    light_code  VARCHAR(30) NOT NULL UNIQUE,
    gps         VARCHAR(40) NOT NULL,
    is_on       BOOLEAN NOT NULL DEFAULT FALSE,
    type_id     BIGINT NOT NULL,
    CONSTRAINT fk_light_type FOREIGN KEY (type_id) REFERENCES light_types (id)
);

-- Policy Rules: Municipal compliance keywords and configurable rule expressions
CREATE TABLE IF NOT EXISTS policy_keywords (
    keyword      VARCHAR(40) PRIMARY KEY,
    description  VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS policy_rules (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name    VARCHAR(100) NOT NULL UNIQUE,
    rule_text    VARCHAR(500) NOT NULL,
    description  VARCHAR(300),
    active       BOOLEAN NOT NULL DEFAULT TRUE
);

-- Telemetry Transmissions: Audit log for incoming smart meter data
CREATE TABLE IF NOT EXISTS meter_transmissions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    meter_id          VARCHAR(40) NOT NULL,
    original_reading  VARCHAR(500) NOT NULL,
    use_compression   BOOLEAN NOT NULL,
    use_encryption    BOOLEAN NOT NULL,
    use_auth          BOOLEAN NOT NULL,
    final_sent        VARCHAR(1000) NOT NULL,
    read_back         VARCHAR(500) NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- System Users: Operator authentication and role-based access
CREATE TABLE IF NOT EXISTS users (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(60)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    full_name      VARCHAR(100) NOT NULL,
    role           VARCHAR(40)  NOT NULL DEFAULT 'OPERATOR',
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
