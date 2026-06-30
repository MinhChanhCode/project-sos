-- Thêm cột completed_quantity vào bảng order_items
ALTER TABLE order_items ADD COLUMN completed_quantity INT NOT NULL DEFAULT 0;

-- Cập nhật dữ liệu hiện có: nếu status là COMPLETED thì completed_quantity = quantity
UPDATE order_items 
SET completed_quantity = quantity 
WHERE status = 'COMPLETED';
