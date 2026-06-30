-- Add unique constraint to ensure only one order item per order + menu item combination
-- This prevents duplicate records at database level
ALTER TABLE order_items 
ADD CONSTRAINT uk_order_items_order_menu UNIQUE (order_id, menu_item_id);
