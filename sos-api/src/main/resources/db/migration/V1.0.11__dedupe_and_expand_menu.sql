-- Keep category names tidy if demo data was imported more than once.
DELETE c
FROM categories c
LEFT JOIN menu_items m ON m.category_id = c.id
JOIN (
    SELECT name, MIN(id) AS keep_id
    FROM categories
    GROUP BY name
) kept ON kept.name = c.name
WHERE c.id <> kept.keep_id
  AND m.id IS NULL;

INSERT INTO categories (name, description, is_active)
SELECT 'Khai vị', 'Các món ăn nhẹ mở đầu bữa ăn', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Khai vị');

INSERT INTO categories (name, description, is_active)
SELECT 'Combo', 'Các phần ăn kết hợp tiện lợi theo nhóm hoặc cá nhân', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Combo');

-- Hide duplicated menu names while keeping historical order/cart references intact.
UPDATE menu_items m
JOIN (
    SELECT LOWER(TRIM(name)) AS normalized_name, MIN(id) AS keep_id
    FROM menu_items
    GROUP BY LOWER(TRIM(name))
) kept ON LOWER(TRIM(m.name)) = kept.normalized_name
SET m.is_active = FALSE
WHERE m.id <> kept.keep_id;

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Gỏi cuốn tôm thịt', 'Gỏi cuốn tươi với tôm, thịt, bún, rau thơm và nước chấm đậu phộng', 42000, '', (SELECT id FROM categories WHERE name = 'Khai vị' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Gỏi cuốn tôm thịt'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Chả giò hải sản', 'Chả giò vàng giòn nhân tôm mực, ăn kèm rau sống và nước mắm chua ngọt', 48000, '', (SELECT id FROM categories WHERE name = 'Khai vị' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Chả giò hải sản'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Salad bò lúc lắc', 'Rau xanh, cà chua bi và bò lúc lắc mềm thơm, sốt mè rang', 59000, '', (SELECT id FROM categories WHERE name = 'Khai vị' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Salad bò lúc lắc'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Khoai tây chiên phô mai', 'Khoai tây chiên giòn phủ bột phô mai, phù hợp ăn nhẹ trước bữa', 35000, '', (SELECT id FROM categories WHERE name = 'Khai vị' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Khoai tây chiên phô mai'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Cơm tấm sườn bì chả', 'Cơm tấm Sài Gòn với sườn nướng, bì, chả trứng và nước mắm pha', 68000, '', (SELECT id FROM categories WHERE name = 'Món chính' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Cơm tấm sườn bì chả'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Mì xào bò', 'Mì trứng xào bò mềm, cải thìa, hành tây và sốt đậm đà', 62000, '', (SELECT id FROM categories WHERE name = 'Món chính' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Mì xào bò'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Lẩu Thái hải sản', 'Lẩu Thái chua cay với tôm, mực, cá viên, nấm và rau ăn kèm', 189000, '', (SELECT id FROM categories WHERE name = 'Món chính' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Lẩu Thái hải sản'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Bánh mì chảo', 'Bánh mì chảo nóng gồm bò, trứng ốp la, pate và xúc xích', 58000, '', (SELECT id FROM categories WHERE name = 'Món chính' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Bánh mì chảo'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Trà tắc xí muội', 'Trà tắc mát lạnh kết hợp xí muội, vị chua ngọt dễ uống', 28000, '', (SELECT id FROM categories WHERE name = 'Đồ uống' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Trà tắc xí muội'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Nước ép cam', 'Cam tươi ép nguyên chất, bổ sung vitamin C', 35000, '', (SELECT id FROM categories WHERE name = 'Đồ uống' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Nước ép cam'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Matcha latte', 'Matcha Nhật pha sữa tươi, vị béo nhẹ và thơm trà xanh', 42000, '', (SELECT id FROM categories WHERE name = 'Đồ uống' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Matcha latte'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Sinh tố bơ', 'Bơ sáp xay cùng sữa, mịn béo và thơm tự nhiên', 45000, '', (SELECT id FROM categories WHERE name = 'Đồ uống' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Sinh tố bơ'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Combo cơm gà và trà tắc', 'Một phần cơm gà xối mỡ kèm trà tắc xí muội mát lạnh', 78000, '', (SELECT id FROM categories WHERE name = 'Combo' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Combo cơm gà và trà tắc'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Combo phở bò và nước cam', 'Phở bò tái nóng hổi kèm nước ép cam tươi', 82000, '', (SELECT id FROM categories WHERE name = 'Combo' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Combo phở bò và nước cam'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Combo khai vị 3 món', 'Gỏi cuốn, chả giò hải sản và khoai tây chiên phô mai cho nhóm nhỏ', 119000, '', (SELECT id FROM categories WHERE name = 'Combo' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Combo khai vị 3 món'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
SELECT 'Combo gia đình 4 người', 'Lẩu Thái hải sản, cơm tấm sườn bì chả, salad bò và 4 ly trà tắc', 329000, '', (SELECT id FROM categories WHERE name = 'Combo' ORDER BY id LIMIT 1), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Combo gia đình 4 người'));
