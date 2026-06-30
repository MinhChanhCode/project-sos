-- TABLE: service_requests
CREATE TABLE service_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id VARCHAR(255) NOT NULL,
    table_name VARCHAR(255),
    session_id VARCHAR(255),
    request_type ENUM('GENERAL_SERVICE', 'FOOD_REQUEST', 'CLEANING', 'PAYMENT', 'OTHER') NOT NULL,
    description TEXT,
    status ENUM('PENDING', 'IN_PROGRESS', 'DONE', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM',
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at DATETIME,
    completed_at DATETIME,
    assigned_to VARCHAR(255),
    notes TEXT,
    created_by VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_table_id (table_id),
    INDEX idx_session_id (session_id),
    INDEX idx_status (status),
    INDEX idx_requested_at (requested_at),
    INDEX idx_assigned_to (assigned_to)
);


