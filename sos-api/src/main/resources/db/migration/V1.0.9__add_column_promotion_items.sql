ALTER TABLE menu_items
  ADD COLUMN original_price DECIMAL(10,2) NULL,
  ADD COLUMN promotional_price DECIMAL(10,2) NULL,
  ADD COLUMN promotion_end_date TIMESTAMP NULL;

