-- =========================================================
-- Medical Management System - Database Schema
-- =========================================================

-- ---------------------------------------------------------
-- users
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    licence_number   VARCHAR(255) NOT NULL UNIQUE,
    email            VARCHAR(255) NOT NULL UNIQUE,
    first_name       VARCHAR(255) NOT NULL,
    last_name        VARCHAR(255) NOT NULL,
    mobile_number    VARCHAR(255) NOT NULL,
    village          VARCHAR(255),
    mandal           VARCHAR(255),
    district         VARCHAR(255),
    state            VARCHAR(255),
    role             VARCHAR(50) NOT NULL,
    password         VARCHAR(255),
    approval_status  VARCHAR(50) NOT NULL,
    enabled          BOOLEAN NOT NULL DEFAULT FALSE,
    approved_by      BIGINT
);

-- ---------------------------------------------------------
-- medicines
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicines (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(255),
    manufacturer  VARCHAR(255),
    category      VARCHAR(255),
    quantity      INT NOT NULL,
    price         DOUBLE NOT NULL,
    expiry_date   DATE
);

-- ---------------------------------------------------------
-- dealer_stock
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS dealer_stock (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    dealer_id   BIGINT,
    medicine_id BIGINT,
    quantity    INT NOT NULL,
    CONSTRAINT fk_dealer_stock_dealer
        FOREIGN KEY (dealer_id) REFERENCES users (id),
    CONSTRAINT fk_dealer_stock_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines (id)
);

-- ---------------------------------------------------------
-- user_stock
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_stock (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT,
    medicine_id BIGINT,
    quantity    INT NOT NULL,
    CONSTRAINT fk_user_stock_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_stock_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines (id)
);

-- ---------------------------------------------------------
-- medicine_requests
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicine_requests (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id        BIGINT,
    dealer_id           BIGINT,
    medicine_id         BIGINT,
    requested_quantity  INT NOT NULL,
    status              VARCHAR(50) NOT NULL,
    note                VARCHAR(255),
    dealer_remark       VARCHAR(255),
    requested_at        TIMESTAMP,
    responded_at        TIMESTAMP,
    CONSTRAINT fk_medicine_requests_requester
        FOREIGN KEY (requester_id) REFERENCES users (id),
    CONSTRAINT fk_medicine_requests_dealer
        FOREIGN KEY (dealer_id) REFERENCES users (id),
    CONSTRAINT fk_medicine_requests_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines (id)
);

-- ---------------------------------------------------------
-- suggestions
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS suggestions (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_user_id BIGINT,
    to_user_id   BIGINT,
    subject      VARCHAR(255),
    message      VARCHAR(1000),
    created_at   TIMESTAMP,
    read_status  BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_suggestions_from_user
        FOREIGN KEY (from_user_id) REFERENCES users (id),
    CONSTRAINT fk_suggestions_to_user
        FOREIGN KEY (to_user_id) REFERENCES users (id)
);

-- ---------------------------------------------------------
-- transactions
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id      BIGINT,
    from_user_id     BIGINT,
    to_user_id       BIGINT,
    quantity         INT NOT NULL,
    total_price      DOUBLE NOT NULL,
    type             VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMP,
    CONSTRAINT fk_transactions_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines (id),
    CONSTRAINT fk_transactions_from_user
        FOREIGN KEY (from_user_id) REFERENCES users (id),
    CONSTRAINT fk_transactions_to_user
        FOREIGN KEY (to_user_id) REFERENCES users (id)
);
