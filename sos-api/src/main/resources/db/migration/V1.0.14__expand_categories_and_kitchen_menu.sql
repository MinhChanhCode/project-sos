INSERT INTO categories (name, description, is_active)
SELECT 'Đồ nướng', 'Các món nướng than/áp chảo phục vụ nóng', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Đồ nướng');

INSERT INTO categories (name, description, is_active)
SELECT 'Lẩu', 'Các món lẩu dùng chung theo nhóm', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Lẩu');

INSERT INTO categories (name, description, is_active)
SELECT 'Bia/nước giải khát', 'Bia, nước ngọt và thức uống đóng chai', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Bia/nước giải khát');

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Ba chỉ bò nướng sốt tiêu', 'Ba chỉ bò nướng mềm, phủ sốt tiêu đen thơm cay nhẹ', 129000, '', (SELECT id FROM categories WHERE name = 'Đồ nướng' ORDER BY id LIMIT 1), TRUE, TRUE, 'GRILL', 'beo, thom, cay nhe', 2, 'ba chỉ bò, tiêu đen, rau nướng', 'nhóm 2-3 người', 'bia lạnh, salad', FALSE, 18
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Ba chỉ bò nướng sốt tiêu'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Sườn heo nướng mật ong', 'Sườn heo nướng mật ong vàng óng, vị ngọt mặn cân bằng', 145000, '', (SELECT id FROM categories WHERE name = 'Đồ nướng' ORDER BY id LIMIT 1), TRUE, TRUE, 'GRILL', 'ngot, man, thom', 1, 'sườn heo, mật ong, mè rang', 'gia đình, nhóm bạn', 'cơm trắng, trà tắc', FALSE, 22
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Sườn heo nướng mật ong'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Gà nướng muối ớt', 'Đùi gà nướng muối ớt da giòn, thịt mềm và đậm vị', 98000, '', (SELECT id FROM categories WHERE name = 'Đồ nướng' ORDER BY id LIMIT 1), TRUE, TRUE, 'GRILL', 'cay, man, dam vi', 3, 'đùi gà, muối ớt, rau răm', 'người thích cay', 'bia, nước ngọt', FALSE, 20
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Gà nướng muối ớt'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Hải sản nướng phô mai', 'Tôm, mực và sò nướng phủ phô mai béo thơm', 169000, '', (SELECT id FROM categories WHERE name = 'Đồ nướng' ORDER BY id LIMIT 1), TRUE, TRUE, 'GRILL', 'beo, thom, hai san', 1, 'tôm, mực, sò, phô mai', 'nhóm 2-4 người', 'soda bạc hà', FALSE, 18
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Hải sản nướng phô mai'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Đậu hũ nướng sa tế', 'Đậu hũ non nướng sa tế, ngoài xém thơm trong mềm béo', 52000, '', (SELECT id FROM categories WHERE name = 'Đồ nướng' ORDER BY id LIMIT 1), TRUE, TRUE, 'GRILL', 'chay, cay nhe, thom', 2, 'đậu hũ, sa tế, hành lá', 'ăn chay, ăn nhẹ', 'trà đào', TRUE, 12
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Đậu hũ nướng sa tế'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Bạch tuộc nướng sa tế', 'Bạch tuộc nướng sa tế giòn ngọt, cay thơm kiểu quán nướng', 155000, '', (SELECT id FROM categories WHERE name = 'Đồ nướng' ORDER BY id LIMIT 1), TRUE, TRUE, 'GRILL', 'cay, hai san, gion', 3, 'bạch tuộc, sa tế, rau thơm', 'nhóm bạn', 'bia lager', FALSE, 20
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Bạch tuộc nướng sa tế'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Lẩu bò nhúng giấm', 'Lẩu bò nhúng giấm chua dịu, ăn kèm rau sống và bún tươi', 219000, '', (SELECT id FROM categories WHERE name = 'Lẩu' ORDER BY id LIMIT 1), TRUE, TRUE, 'HOTPOT', 'chua diu, thanh, bo', 1, 'bò, giấm, rau sống, bún', 'nhóm 3-4 người', 'nước ép cam', FALSE, 25
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Lẩu bò nhúng giấm'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Lẩu gà lá é', 'Lẩu gà lá é thơm nhẹ, nước dùng ngọt thanh và ấm bụng', 199000, '', (SELECT id FROM categories WHERE name = 'Lẩu' ORDER BY id LIMIT 1), TRUE, TRUE, 'HOTPOT', 'thanh, thom, am', 1, 'gà, lá é, nấm, bún', 'gia đình', 'trà gừng mật ong', FALSE, 25
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Lẩu gà lá é'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Lẩu nấm chay', 'Lẩu nấm chay với nước dùng rau củ, nhiều loại nấm và đậu hũ', 179000, '', (SELECT id FROM categories WHERE name = 'Lẩu' ORDER BY id LIMIT 1), TRUE, TRUE, 'HOTPOT', 'chay, thanh, nam', 0, 'nấm, rau củ, đậu hũ', 'ăn chay, nhóm nhẹ vị', 'trà vải', TRUE, 22
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Lẩu nấm chay'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Lẩu kim chi bò Mỹ', 'Lẩu kim chi cay nhẹ với bò Mỹ, nấm kim châm và đậu hũ', 239000, '', (SELECT id FROM categories WHERE name = 'Lẩu' ORDER BY id LIMIT 1), TRUE, TRUE, 'HOTPOT', 'cay, chua, bo', 3, 'kim chi, bò Mỹ, nấm, đậu hũ', 'người thích cay', 'bia lạnh', FALSE, 25
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Lẩu kim chi bò Mỹ'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Lẩu cua đồng', 'Lẩu cua đồng dân dã với riêu cua, rau mồng tơi và bún', 229000, '', (SELECT id FROM categories WHERE name = 'Lẩu' ORDER BY id LIMIT 1), TRUE, TRUE, 'HOTPOT', 'dan da, beo nhe, thanh', 1, 'cua đồng, riêu cua, rau, bún', 'gia đình', 'trà chanh', FALSE, 28
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Lẩu cua đồng'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Bia lager lon', 'Bia lager lon 330ml ướp lạnh', 28000, '', (SELECT id FROM categories WHERE name = 'Bia/nước giải khát' ORDER BY id LIMIT 1), TRUE, TRUE, 'DRINK', 'mat lanh, nhe', 0, 'bia lager', 'người lớn', 'đồ nướng', FALSE, 1
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Bia lager lon'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Bia thủ công IPA', 'Bia thủ công IPA vị đắng nhẹ, hương trái cây', 65000, '', (SELECT id FROM categories WHERE name = 'Bia/nước giải khát' ORDER BY id LIMIT 1), TRUE, TRUE, 'DRINK', 'dang nhe, trai cay', 0, 'bia IPA', 'người lớn', 'sườn nướng, bạch tuộc', FALSE, 1
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Bia thủ công IPA'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Nước suối', 'Nước suối đóng chai 500ml', 12000, '', (SELECT id FROM categories WHERE name = 'Bia/nước giải khát' ORDER BY id LIMIT 1), TRUE, TRUE, 'DRINK', 'mat, tinh khiet', 0, 'nước khoáng', 'mọi khách hàng', 'mọi món', TRUE, 1
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Nước suối'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Nước ngọt cola', 'Cola lon 330ml ướp lạnh, vị ngọt sảng khoái', 22000, '', (SELECT id FROM categories WHERE name = 'Bia/nước giải khát' ORDER BY id LIMIT 1), TRUE, TRUE, 'DRINK', 'ngot, co gas, mat', 0, 'cola', 'nhóm bạn, trẻ em', 'combo, đồ nướng', TRUE, 1
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Nước ngọt cola'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Nước chanh sả', 'Nước chanh sả mát lạnh, thơm nhẹ và giải ngấy tốt', 32000, '', (SELECT id FROM categories WHERE name = 'Bia/nước giải khát' ORDER BY id LIMIT 1), TRUE, TRUE, 'DRINK', 'chua nhe, thom, mat', 0, 'chanh, sả, đường', 'mọi khách hàng', 'lẩu, đồ nướng', TRUE, 3
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Nước chanh sả'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Combo nướng đôi', 'Ba chỉ bò nướng, đậu hũ nướng sa tế và 2 nước chanh sả', 229000, '', (SELECT id FROM categories WHERE name = 'Combo' ORDER BY id LIMIT 1), TRUE, TRUE, 'COMBO', 'nuong, combo, tiet kiem', 2, 'ba chỉ bò, đậu hũ, nước chanh sả', '2 người', 'dùng nhanh buổi tối', FALSE, 20
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Combo nướng đôi'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Combo lẩu sum vầy', 'Lẩu gà lá é, gỏi cuốn và 4 nước suối cho nhóm gia đình', 299000, '', (SELECT id FROM categories WHERE name = 'Combo' ORDER BY id LIMIT 1), TRUE, TRUE, 'COMBO', 'lau, gia dinh, tiet kiem', 1, 'lẩu gà, gỏi cuốn, nước suối', '4 người', 'gia đình', FALSE, 25
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Combo lẩu sum vầy'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Cơm bò nướng trứng', 'Cơm nóng dùng với bò nướng sốt tiêu, trứng ốp la và đồ chua', 79000, '', (SELECT id FROM categories WHERE name = 'Món chính' ORDER BY id LIMIT 1), TRUE, TRUE, 'MAIN', 'dam vi, bo, no lau', 1, 'cơm, bò nướng, trứng, đồ chua', 'ăn trưa, ăn tối', 'trà tắc', FALSE, 15
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Cơm bò nướng trứng'));

INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active, type, taste_tags, spicy_level, ingredients, suitable_for, pairing, is_vegetarian, prep_time_minutes)
SELECT 'Mì cay hải sản', 'Mì cay hải sản với tôm, mực, nấm và nước dùng cay đậm', 89000, '', (SELECT id FROM categories WHERE name = 'Món chính' ORDER BY id LIMIT 1), TRUE, TRUE, 'MAIN', 'cay, hai san, nong', 4, 'mì, tôm, mực, nấm', 'người thích cay', 'nước suối', FALSE, 14
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE LOWER(TRIM(name)) = LOWER('Mì cay hải sản'));
