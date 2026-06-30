-- Add per-status quantity columns and migrate data, then drop old quantity
ALTER TABLE order_items
    ADD COLUMN pending_quantity INT NOT NULL DEFAULT 0,
    ADD COLUMN preparing_quantity INT NOT NULL DEFAULT 0,
    ADD COLUMN served_quantity INT NOT NULL DEFAULT 0,
    ADD COLUMN cancelled_quantity INT NOT NULL DEFAULT 0,
    ADD COLUMN out_of_stock_quantity INT NOT NULL DEFAULT 0;

-- Migrate existing data from quantity/status to the corresponding new columns
UPDATE order_items SET pending_quantity = quantity WHERE status = 'PENDING';
UPDATE order_items SET preparing_quantity = quantity WHERE status = 'PREPARING';
-- completed_quantity column already exists; ensure it reflects previous quantity when status was COMPLETED or SERVED
UPDATE order_items SET completed_quantity = quantity WHERE status IN ('COMPLETED','SERVED');
UPDATE order_items SET served_quantity = quantity WHERE status = 'SERVED';
UPDATE order_items SET cancelled_quantity = quantity WHERE status = 'CANCELLED';
UPDATE order_items SET out_of_stock_quantity = quantity WHERE status = 'OUT_OF_STOCK';

-- Finally, drop the old quantity column
ALTER TABLE order_items DROP COLUMN quantity;

