-- Areas
CREATE TABLE areas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Extend tables with area, position, status
ALTER TABLE tables ADD COLUMN area_id BIGINT NULL;
ALTER TABLE tables ADD COLUMN pos_x INT DEFAULT 0;
ALTER TABLE tables ADD COLUMN pos_y INT DEFAULT 0;
ALTER TABLE tables ADD COLUMN table_status VARCHAR(50) DEFAULT 'EMPTY';
ALTER TABLE tables ADD CONSTRAINT fk_tables_area FOREIGN KEY (area_id) REFERENCES areas(id);

-- User profile
ALTER TABLE user ADD COLUMN full_name VARCHAR(255);
ALTER TABLE user ADD COLUMN phone VARCHAR(50);
ALTER TABLE user ADD COLUMN email VARCHAR(255);

-- QR codes
CREATE TABLE qr_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BINARY(16) NOT NULL,
    code_url TEXT,
    qr_token VARCHAR(255) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (table_id) REFERENCES tables(id)
);

-- Reviews & sentiment
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BINARY(16),
    session_id VARCHAR(255),
    rating INT NOT NULL,
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (table_id) REFERENCES tables(id)
);

CREATE TABLE sentiment_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT NOT NULL UNIQUE,
    sentiment VARCHAR(50) NOT NULL,
    confidence DECIMAL(5, 4),
    analyzed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (review_id) REFERENCES reviews(id)
);

-- Invoices
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    table_id BINARY(16),
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0,
    tax DECIMAL(12, 2) NOT NULL DEFAULT 0,
    discount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total DECIMAL(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    paid_at DATETIME,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (table_id) REFERENCES tables(id)
);

-- Employees & assignments
CREATE TABLE employees (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    position VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BINARY(16) NOT NULL,
    area_id BIGINT,
    shift_id BIGINT,
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (area_id) REFERENCES areas(id),
    FOREIGN KEY (shift_id) REFERENCES staff_shifts(id)
);

-- Chat histories
CREATE TABLE chat_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    user_message TEXT NOT NULL,
    bot_response TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Menu embeddings metadata (vectors stored in AI service)
CREATE TABLE menu_embeddings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_item_id BIGINT NOT NULL UNIQUE,
    content_hash VARCHAR(64),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- Seed roles
INSERT INTO role (name, created_at) SELECT 'KITCHEN', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'KITCHEN');
INSERT INTO role (name, created_at) SELECT 'MANAGER', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'MANAGER');

-- Default area
INSERT INTO areas (name, description, is_active) VALUES ('Tầng 1', 'Khu vực chính', TRUE);
