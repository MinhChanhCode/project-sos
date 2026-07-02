INSERT INTO categories (name, description, is_active)
SELECT 'Món chính', 'Các món ăn chính dùng trong bữa', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Món chính');

INSERT INTO categories (name, description, is_active)
SELECT 'Đồ uống', 'Nước uống pha chế và giải khát', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Đồ uống');

INSERT INTO categories (name, description, is_active)
SELECT 'Khai vị', 'Các món ăn nhẹ mở đầu bữa ăn', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Khai vị');

INSERT INTO categories (name, description, is_active)
SELECT 'Combo', 'Các phần ăn kết hợp tiện lợi theo nhóm hoặc cá nhân', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Combo');

UPDATE menu_items
SET category_id = (SELECT id FROM categories WHERE name = 'Món chính' ORDER BY id LIMIT 1)
WHERE category_id IS NULL
  AND name IN ('Cơm tấm sườn bì chả', 'Mì xào bò', 'Lẩu Thái hải sản', 'Bánh mì chảo', 'Cơm bò nướng trứng', 'Mì cay hải sản');

UPDATE menu_items
SET category_id = (SELECT id FROM categories WHERE name = 'Đồ uống' ORDER BY id LIMIT 1)
WHERE category_id IS NULL
  AND name IN ('Trà tắc xí muội', 'Nước ép cam', 'Matcha latte', 'Sinh tố bơ');

UPDATE menu_items
SET category_id = (SELECT id FROM categories WHERE name = 'Combo' ORDER BY id LIMIT 1)
WHERE category_id IS NULL
  AND name LIKE 'Combo%';
