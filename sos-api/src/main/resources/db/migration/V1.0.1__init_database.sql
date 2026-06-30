-- TABLE: role
CREATE TABLE role (
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NULL,
    created_by VARCHAR(255),
    created_at DATETIME,
    updated_by VARCHAR(255),
    updated_at DATETIME,
    CONSTRAINT pk_role PRIMARY KEY (id)
);

-- TABLE: user (UUID)
CREATE TABLE user (
    id       BINARY(16) PRIMARY KEY,
    username VARCHAR(255)          NULL,
    password VARCHAR(255)          NULL,
    created_by VARCHAR(255),
    created_at DATETIME,
    updated_by VARCHAR(255),
    updated_at DATETIME,
    CONSTRAINT uc_user_username UNIQUE (username)
);

-- TABLE: user_roles
CREATE TABLE user_roles (
    role_id BIGINT NOT NULL,
    user_id BINARY(16) NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (role_id, user_id),
    CONSTRAINT uc_user_roles_unique UNIQUE (role_id, user_id)
);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES role (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES user (id);

-- TABLE: tables (UUID)
CREATE TABLE tables (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(255),
    capacity INT,
    is_available BOOLEAN
);

-- TABLE: categories
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    is_active BOOLEAN
);

-- TABLE: menu_items
CREATE TABLE menu_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    price DECIMAL(10, 2),
    image_url TEXT,
    category_id BIGINT,
    is_available BOOLEAN,
    is_active BOOLEAN,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- TABLE: orders
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BINARY(16),
    status VARCHAR(50),
    created_at DATETIME,
    completed_at DATETIME,
    FOREIGN KEY (table_id) REFERENCES tables(id)
);

-- TABLE: order_items
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    menu_item_id BIGINT,
    quantity INT,
    status VARCHAR(50),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- TABLE: order_staff
CREATE TABLE order_staff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    staff_id BINARY(16),
    role VARCHAR(50),
    assigned_at DATETIME,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (staff_id) REFERENCES user(id),
    CONSTRAINT uc_order_staff_unique UNIQUE (order_id, staff_id)
);

-- TABLE: staff_shifts
CREATE TABLE staff_shifts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id BINARY(16),
    start_time DATETIME,
    end_time DATETIME,
    role VARCHAR(50),
    is_completed BOOLEAN,
    FOREIGN KEY (staff_id) REFERENCES user(id)
);

-- TABLE: notifications
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id BINARY(16),
    title VARCHAR(255),
    message TEXT,
    type VARCHAR(50),
    priority VARCHAR(50),
    is_read BOOLEAN,
    read_at DATETIME,
    metadata JSON,
    FOREIGN KEY (recipient_id) REFERENCES user(id)
);

-- TABLE: payments (UUID)
CREATE TABLE payments (
    id BINARY(16) PRIMARY KEY,
    order_id BIGINT UNIQUE,
    amount DECIMAL(10, 2),
    method VARCHAR(50),
    status VARCHAR(50),
    paid_at DATETIME,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- TABLE: carts
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BINARY(16),
    session_id VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (table_id) REFERENCES tables(id)
);

-- TABLE: cart_items
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT,
    menu_item_id BIGINT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    notes TEXT,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id),
    CONSTRAINT uc_cart_items_unique UNIQUE (cart_id, menu_item_id)
);