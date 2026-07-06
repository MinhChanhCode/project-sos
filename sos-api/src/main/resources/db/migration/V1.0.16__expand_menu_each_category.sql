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

INSERT INTO categories (name, description, is_active)
SELECT 'Đồ nướng', 'Các món nướng than/áp chảo phục vụ nóng', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Đồ nướng');

INSERT INTO categories (name, description, is_active)
SELECT 'Lẩu', 'Các món lẩu dùng chung theo nhóm', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Lẩu');

INSERT INTO categories (name, description, is_active)
SELECT 'Bia/nước giải khát', 'Bia, nước ngọt và thức uống đóng chai', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Bia/nước giải khát');

INSERT INTO menu_items (
    name, description, price, image_url, category_id, is_available, is_active,
    type, taste_tags, spicy_level, ingredients, allergens, suitable_for, pairing,
    is_vegetarian, prep_time_minutes
)
SELECT s.name, s.description, s.price, '', c.id, TRUE, TRUE, s.type, s.taste_tags,
       s.spicy_level, s.ingredients, s.allergens, s.suitable_for, s.pairing,
       s.is_vegetarian, s.prep_time_minutes
FROM (
    SELECT 'Cơm chiên hải sản' name, 'Cơm chiên tơi hạt với tôm, mực, trứng và rau củ' description, 79000 price, 'Món chính' category_name, 'MAIN' type, 'dam vi, hai san, no lau' taste_tags, 1 spicy_level, 'cơm, tôm, mực, trứng, rau củ' ingredients, 'hải sản, trứng' allergens, 'ăn trưa, ăn tối' suitable_for, 'trà tắc xí muội' pairing, FALSE is_vegetarian, 14 prep_time_minutes
    UNION ALL SELECT 'Cơm sườn nướng ngũ vị', 'Sườn nướng ngũ vị ăn cùng cơm nóng, đồ chua và nước mắm', 85000, 'Món chính', 'MAIN', 'thom, man ngot, dam vi', 1, 'cơm, sườn heo, đồ chua', '', 'người thích món nướng', 'nước chanh sả', FALSE, 16
    UNION ALL SELECT 'Cơm gà teriyaki', 'Đùi gà áp chảo sốt teriyaki dùng với cơm Nhật và salad', 89000, 'Món chính', 'MAIN', 'ngot man, mem, de an', 0, 'cơm, gà, sốt teriyaki, salad', 'đậu nành', 'trẻ em, người không ăn cay', 'trà đào', FALSE, 16
    UNION ALL SELECT 'Cơm cá hồi áp chảo', 'Cá hồi áp chảo sốt bơ chanh ăn cùng cơm và rau củ', 129000, 'Món chính', 'MAIN', 'beo nhe, thanh, cao cap', 0, 'cơm, cá hồi, bơ, chanh, rau củ', 'cá, sữa', 'ăn tối, khách thích món nhẹ', 'nước ép cam', FALSE, 18
    UNION ALL SELECT 'Cơm bò lúc lắc', 'Bò lúc lắc mềm thơm ăn cùng cơm, khoai tây và salad', 99000, 'Món chính', 'MAIN', 'bo, dam vi, no lau', 1, 'cơm, bò, khoai tây, salad', '', 'người cần ăn no', 'cola lạnh', FALSE, 15
    UNION ALL SELECT 'Bún thịt nướng chả giò', 'Bún tươi với thịt nướng, chả giò, rau sống và nước mắm', 72000, 'Món chính', 'MAIN', 'thom, tuoi, de an', 1, 'bún, thịt nướng, chả giò, rau sống', 'trứng' , 'ăn trưa, ăn nhẹ', 'trà chanh', FALSE, 12
    UNION ALL SELECT 'Bún bò xào sả', 'Bún bò xào sả ớt thơm nồng, rau sống và đậu phộng', 78000, 'Món chính', 'MAIN', 'thom sa, cay nhe, bo', 2, 'bún, bò, sả, rau sống, đậu phộng', 'đậu phộng', 'người thích vị đậm', 'nước suối', FALSE, 14
    UNION ALL SELECT 'Mì Ý bò bằm', 'Mì Ý sốt bò bằm cà chua, phô mai nhẹ và lá thơm', 88000, 'Món chính', 'MAIN', 'chua nhe, beo, tay', 0, 'mì Ý, bò bằm, cà chua, phô mai', 'sữa, gluten', 'trẻ em, khách không ăn cay', 'soda chanh', FALSE, 16
    UNION ALL SELECT 'Mì Ý hải sản cay', 'Mì Ý hải sản sốt cà chua cay nhẹ với tôm, mực và nghêu', 115000, 'Món chính', 'MAIN', 'hai san, cay nhe, chua', 2, 'mì Ý, tôm, mực, nghêu, cà chua', 'hải sản, gluten', 'người thích hải sản', 'bia lager', FALSE, 18
    UNION ALL SELECT 'Phở gà xé', 'Phở gà xé nước dùng thanh, hành lá và rau thơm', 62000, 'Món chính', 'MAIN', 'thanh, am, de an', 0, 'phở, gà, hành, rau thơm', '', 'bữa sáng, trẻ em', 'trà gừng mật ong', FALSE, 10
    UNION ALL SELECT 'Hủ tiếu Nam Vang', 'Hủ tiếu nước với tôm, thịt, trứng cút và nước dùng ngọt thanh', 76000, 'Món chính', 'MAIN', 'thanh, hai san, no vua', 0, 'hủ tiếu, tôm, thịt, trứng cút', 'hải sản, trứng', 'ăn sáng, ăn trưa', 'nước ép ổi', FALSE, 12
    UNION ALL SELECT 'Bánh canh cua', 'Bánh canh cua sánh nhẹ, thịt cua và chả cá thơm ngon', 88000, 'Món chính', 'MAIN', 'beo nhe, cua, nong', 0, 'bánh canh, cua, chả cá', 'hải sản', 'khách thích món nước', 'trà tắc', FALSE, 14
    UNION ALL SELECT 'Miến gà nấm', 'Miến gà với nấm hương, nước dùng thanh và rau thơm', 69000, 'Món chính', 'MAIN', 'thanh, nhe bung, am', 0, 'miến, gà, nấm hương', '', 'người ăn nhẹ', 'nước suối', FALSE, 12
    UNION ALL SELECT 'Cá kho tộ cơm trắng', 'Cá kho tộ đậm vị ăn cùng cơm trắng và rau luộc', 95000, 'Món chính', 'MAIN', 'dam vi, truyen thong, man ngot', 1, 'cá, nước mắm, cơm, rau luộc', 'cá', 'bữa cơm gia đình', 'trà đá', FALSE, 18
    UNION ALL SELECT 'Gà kho gừng cơm nóng', 'Gà kho gừng ấm bụng, vị mặn ngọt hài hòa', 82000, 'Món chính', 'MAIN', 'am, gung, dam vi', 1, 'gà, gừng, cơm', '', 'ăn tối, ngày mưa', 'trà gừng', FALSE, 16
    UNION ALL SELECT 'Bò kho bánh mì', 'Bò kho mềm với cà rốt, nước sốt đậm và bánh mì giòn', 79000, 'Món chính', 'MAIN', 'dam vi, am, bo', 1, 'bò, cà rốt, bánh mì', 'gluten', 'bữa sáng, ăn no', 'cà phê sữa đá', FALSE, 16
    UNION ALL SELECT 'Cơm chay rau củ sốt nấm', 'Cơm chay với rau củ, đậu hũ và sốt nấm thanh nhẹ', 69000, 'Món chính', 'MAIN', 'chay, thanh, it beo', 0, 'cơm, rau củ, đậu hũ, nấm', 'đậu nành', 'ăn chay, người ăn lành mạnh', 'trà vải', TRUE, 14
    UNION ALL SELECT 'Bún riêu chay', 'Bún riêu chay từ đậu hũ, nấm và cà chua, vị thanh nhẹ', 65000, 'Món chính', 'MAIN', 'chay, chua nhe, thanh', 0, 'bún, đậu hũ, nấm, cà chua', 'đậu nành', 'ăn chay', 'nước chanh sả', TRUE, 14
    UNION ALL SELECT 'Mì xào rau củ chay', 'Mì xào rau củ giòn ngọt, sốt nấm nhẹ và mè rang', 62000, 'Món chính', 'MAIN', 'chay, gion, de an', 0, 'mì, rau củ, nấm, mè', 'gluten, mè', 'ăn chay, trẻ em', 'trà đào', TRUE, 12
    UNION ALL SELECT 'Cơm tôm rim nước dừa', 'Tôm rim nước dừa đậm vị ăn cùng cơm nóng và dưa leo', 92000, 'Món chính', 'MAIN', 'hai san, ngot man, dam vi', 1, 'cơm, tôm, nước dừa, dưa leo', 'hải sản', 'ăn trưa, ăn tối', 'nước ép cam', FALSE, 15

    UNION ALL SELECT 'Gỏi xoài tôm khô', 'Gỏi xoài xanh trộn tôm khô, rau răm và đậu phộng', 59000, 'Khai vị', 'APPETIZER', 'chua, cay nhe, gion', 2, 'xoài xanh, tôm khô, rau răm, đậu phộng', 'hải sản, đậu phộng', 'khai vị nhóm', 'bia lager', FALSE, 8
    UNION ALL SELECT 'Gỏi ngó sen tôm thịt', 'Ngó sen giòn trộn tôm thịt, rau thơm và nước mắm chua ngọt', 69000, 'Khai vị', 'APPETIZER', 'chua ngot, thanh, gion', 1, 'ngó sen, tôm, thịt, rau thơm', 'hải sản', 'mở đầu bữa ăn', 'trà tắc', FALSE, 10
    UNION ALL SELECT 'Gỏi bò bóp thấu', 'Bò mềm trộn hành tây, rau thơm và sốt chua ngọt', 85000, 'Khai vị', 'APPETIZER', 'bo, chua ngot, thom', 1, 'bò, hành tây, rau thơm', '', 'nhóm bạn', 'bia IPA', FALSE, 10
    UNION ALL SELECT 'Salad cá ngừ', 'Salad rau xanh, cá ngừ, bắp ngọt và sốt mè rang', 72000, 'Khai vị', 'APPETIZER', 'thanh, it beo, ca', 0, 'rau xanh, cá ngừ, bắp, mè', 'cá, mè', 'ăn nhẹ, khách healthy', 'nước ép ổi', FALSE, 8
    UNION ALL SELECT 'Salad ức gà áp chảo', 'Ức gà áp chảo ăn cùng rau xanh, cà chua và sốt yogurt', 78000, 'Khai vị', 'APPETIZER', 'healthy, thanh, gà', 0, 'ức gà, rau xanh, yogurt', 'sữa', 'người ăn lành mạnh', 'sinh tố bơ', FALSE, 10
    UNION ALL SELECT 'Khoai lang kén', 'Khoai lang kén chiên giòn, vị ngọt bùi dễ ăn', 42000, 'Khai vị', 'APPETIZER', 'ngot, gion, an vat', 0, 'khoai lang, bột, mè', 'gluten, mè', 'trẻ em, ăn nhẹ', 'trà sữa', TRUE, 8
    UNION ALL SELECT 'Phô mai que', 'Phô mai que chiên giòn, kéo sợi béo thơm', 49000, 'Khai vị', 'APPETIZER', 'beo, gion, tre em', 0, 'phô mai, bột chiên', 'sữa, gluten', 'trẻ em, nhóm bạn', 'cola', TRUE, 7
    UNION ALL SELECT 'Mực chiên giòn', 'Mực khoanh chiên giòn ăn cùng sốt tartar', 89000, 'Khai vị', 'APPETIZER', 'hai san, gion, beo nhe', 0, 'mực, bột chiên, sốt tartar', 'hải sản, trứng', 'khai vị, nhậu nhẹ', 'bia lager', FALSE, 10
    UNION ALL SELECT 'Tôm tempura', 'Tôm tempura giòn nhẹ, ăn cùng sốt chấm kiểu Nhật', 99000, 'Khai vị', 'APPETIZER', 'hai san, gion nhe', 0, 'tôm, bột tempura, sốt chấm', 'hải sản, gluten', 'khai vị cao cấp', 'soda bạc hà', FALSE, 10
    UNION ALL SELECT 'Cánh gà chiên nước mắm', 'Cánh gà chiên giòn áo sốt nước mắm tỏi thơm', 79000, 'Khai vị', 'APPETIZER', 'dam vi, gion, man ngot', 1, 'cánh gà, nước mắm, tỏi', '', 'nhóm bạn', 'cola lạnh', FALSE, 12
    UNION ALL SELECT 'Nem nướng cuốn rau', 'Nem nướng ăn kèm rau sống, bánh tráng và sốt đậu phộng', 79000, 'Khai vị', 'APPETIZER', 'thom, cuon, dam vi', 1, 'nem nướng, rau sống, bánh tráng, đậu phộng', 'đậu phộng', 'khai vị nhóm', 'trà đào', FALSE, 12
    UNION ALL SELECT 'Bánh xèo mini', 'Bánh xèo mini giòn thơm, nhân tôm thịt và giá', 68000, 'Khai vị', 'APPETIZER', 'gion, dan da, thom', 0, 'bột gạo, tôm, thịt, giá', 'hải sản', 'ăn chơi, nhóm nhỏ', 'trà tắc', FALSE, 12
    UNION ALL SELECT 'Há cảo tôm hấp', 'Há cảo tôm hấp nóng, vỏ mỏng và nhân ngọt', 62000, 'Khai vị', 'APPETIZER', 'hai san, mem, nong', 0, 'tôm, bột há cảo', 'hải sản, gluten', 'trẻ em, khai vị', 'trà gừng', FALSE, 8
    UNION ALL SELECT 'Xíu mại sốt cà', 'Xíu mại thịt heo sốt cà chua, ăn kèm bánh mì nhỏ', 58000, 'Khai vị', 'APPETIZER', 'chua nhe, mem, de an', 0, 'thịt heo, cà chua, bánh mì', 'gluten', 'trẻ em, ăn nhẹ', 'nước ép cam', FALSE, 10
    UNION ALL SELECT 'Đậu hũ non sốt trứng muối', 'Đậu hũ non chiên nhẹ phủ sốt trứng muối béo thơm', 62000, 'Khai vị', 'APPETIZER', 'beo, trung muoi, mem', 0, 'đậu hũ, trứng muối', 'trứng, đậu nành', 'khai vị, ăn chay trứng', 'trà chanh', TRUE, 10
    UNION ALL SELECT 'Rau củ luộc kho quẹt', 'Rau củ luộc giòn ngọt ăn cùng kho quẹt đậm vị', 59000, 'Khai vị', 'APPETIZER', 'thanh, dam vi, dan da', 0, 'rau củ, kho quẹt', '', 'gia đình, người thích món Việt', 'trà đá', FALSE, 9
    UNION ALL SELECT 'Chạo tôm mía', 'Chạo tôm quấn mía nướng thơm, vị ngọt tự nhiên', 88000, 'Khai vị', 'APPETIZER', 'hai san, thom, ngot', 0, 'tôm, mía, gia vị', 'hải sản', 'khai vị nhóm', 'bia thủ công IPA', FALSE, 12
    UNION ALL SELECT 'Bò cuốn lá lốt', 'Bò cuốn lá lốt nướng thơm, ăn kèm rau sống', 82000, 'Khai vị', 'APPETIZER', 'bo, thom la lot, dam vi', 1, 'bò, lá lốt, rau sống', '', 'nhóm bạn', 'bia lager', FALSE, 12
    UNION ALL SELECT 'Súp bí đỏ kem tươi', 'Súp bí đỏ mịn béo nhẹ với kem tươi và bánh mì giòn', 52000, 'Khai vị', 'APPETIZER', 'beo nhe, nong, tre em', 0, 'bí đỏ, kem tươi, bánh mì', 'sữa, gluten', 'trẻ em, người ăn nhẹ', 'nước suối', TRUE, 8
    UNION ALL SELECT 'Súp cua trứng cút', 'Súp cua nóng với trứng cút, nấm và thịt cua', 59000, 'Khai vị', 'APPETIZER', 'nong, cua, mem', 0, 'cua, trứng cút, nấm', 'hải sản, trứng', 'khai vị, trẻ em', 'trà gừng', FALSE, 8

    UNION ALL SELECT 'Trà sen vàng', 'Trà sen thơm nhẹ, kem sữa mặn và hạt sen bùi', 45000, 'Đồ uống', 'DRINK', 'thom, beo nhe, tra', 0, 'trà sen, kem sữa, hạt sen', 'sữa', 'giải khát, tráng miệng', 'bánh flan', FALSE, 4
    UNION ALL SELECT 'Trà ô long vải', 'Ô long thanh nhẹ kết hợp vải ngọt thơm', 42000, 'Đồ uống', 'DRINK', 'thanh, trai cay, mat', 0, 'ô long, vải, đường', '', 'mọi khách hàng', 'gỏi cuốn', TRUE, 3
    UNION ALL SELECT 'Trà dâu tằm', 'Trà trái cây dâu tằm chua ngọt, mát lạnh', 39000, 'Đồ uống', 'DRINK', 'chua ngot, trai cay, mat', 0, 'trà, dâu tằm', '', 'giải khát', 'món chiên', TRUE, 3
    UNION ALL SELECT 'Trà mãng cầu', 'Trà mãng cầu thơm béo nhẹ, vị chua ngọt dễ uống', 45000, 'Đồ uống', 'DRINK', 'chua ngot, thom, trai cay', 0, 'trà, mãng cầu', '', 'khách thích trái cây', 'đồ nướng', TRUE, 4
    UNION ALL SELECT 'Trà chanh dây', 'Trà chanh dây mát lạnh, vị chua thanh giải ngấy', 38000, 'Đồ uống', 'DRINK', 'chua, thanh, mat', 0, 'trà, chanh dây', '', 'giải ngấy', 'lẩu, đồ nướng', TRUE, 3
    UNION ALL SELECT 'Nước ép dưa hấu', 'Nước ép dưa hấu tươi mát, ít ngọt', 39000, 'Đồ uống', 'DRINK', 'mat, thanh, trai cay', 0, 'dưa hấu', '', 'trẻ em, khách healthy', 'món cay', TRUE, 3
    UNION ALL SELECT 'Nước ép cà rốt cam', 'Nước ép cà rốt cam giàu vitamin, vị ngọt thanh', 43000, 'Đồ uống', 'DRINK', 'healthy, cam, thanh', 0, 'cà rốt, cam', '', 'khách ăn lành mạnh', 'salad', TRUE, 4
    UNION ALL SELECT 'Nước ép táo xanh', 'Nước ép táo xanh chua nhẹ, thơm mát', 45000, 'Đồ uống', 'DRINK', 'chua nhe, trai cay, mat', 0, 'táo xanh', '', 'giải khát', 'mì Ý', TRUE, 3
    UNION ALL SELECT 'Nước ép cần tây táo', 'Nước ép cần tây táo ít ngọt, thanh lọc nhẹ', 49000, 'Đồ uống', 'DRINK', 'healthy, it ngot, thanh', 0, 'cần tây, táo', '', 'khách healthy', 'cơm chay', TRUE, 4
    UNION ALL SELECT 'Sinh tố dâu', 'Sinh tố dâu sữa chua mịn, vị chua ngọt', 49000, 'Đồ uống', 'DRINK', 'beo nhe, dau, mat', 0, 'dâu, sữa chua, sữa', 'sữa', 'tráng miệng', 'bánh su kem', FALSE, 4
    UNION ALL SELECT 'Sinh tố mãng cầu', 'Sinh tố mãng cầu béo mịn, thơm đặc trưng', 52000, 'Đồ uống', 'DRINK', 'beo, thom, trai cay', 0, 'mãng cầu, sữa', 'sữa', 'tráng miệng', 'món cay', FALSE, 4
    UNION ALL SELECT 'Sữa chua việt quất', 'Sữa chua uống việt quất mát lạnh, vị chua nhẹ', 46000, 'Đồ uống', 'DRINK', 'chua nhe, beo, mat', 0, 'sữa chua, việt quất', 'sữa', 'trẻ em, tráng miệng', 'khoai lang kén', FALSE, 3
    UNION ALL SELECT 'Cacao đá xay', 'Cacao đá xay phủ kem, vị đậm và béo', 52000, 'Đồ uống', 'DRINK', 'beo, cacao, ngot', 0, 'cacao, sữa, kem', 'sữa', 'tráng miệng', 'bánh flan', FALSE, 5
    UNION ALL SELECT 'Latte caramel đá', 'Latte caramel đá thơm cà phê, vị ngọt béo cân bằng', 55000, 'Đồ uống', 'DRINK', 'ca phe, beo, ngot', 0, 'cà phê, sữa, caramel', 'sữa', 'người thích cà phê', 'bánh mì chảo', FALSE, 4
    UNION ALL SELECT 'Americano đá', 'Americano đá vị cà phê nguyên bản, ít ngọt', 35000, 'Đồ uống', 'DRINK', 'ca phe, dam, it ngot', 0, 'cà phê', '', 'người thích cà phê', 'bò kho bánh mì', TRUE, 3
    UNION ALL SELECT 'Bạc xỉu', 'Bạc xỉu sữa đá béo thơm, cà phê nhẹ', 39000, 'Đồ uống', 'DRINK', 'beo, sua, ca phe nhe', 0, 'cà phê, sữa đặc', 'sữa', 'khách thích ngọt', 'bánh mì chảo', FALSE, 3
    UNION ALL SELECT 'Soda việt quất', 'Soda việt quất có gas, chua ngọt sảng khoái', 42000, 'Đồ uống', 'DRINK', 'co gas, chua ngot, mat', 0, 'soda, việt quất', '', 'giải khát', 'món chiên', TRUE, 3
    UNION ALL SELECT 'Soda chanh dây', 'Soda chanh dây mát lạnh, vị chua thanh', 42000, 'Đồ uống', 'DRINK', 'co gas, chua, mat', 0, 'soda, chanh dây', '', 'giải ngấy', 'đồ nướng', TRUE, 3
    UNION ALL SELECT 'Mocktail nhiệt đới', 'Mocktail trái cây nhiệt đới, không cồn, thơm mát', 59000, 'Đồ uống', 'DRINK', 'trai cay, mat, khong con', 0, 'dứa, cam, chanh dây, soda', '', 'gia đình, nhóm bạn', 'combo gia đình', TRUE, 5
    UNION ALL SELECT 'Trà gạo rang sữa', 'Trà gạo rang thơm bùi pha sữa béo nhẹ', 45000, 'Đồ uống', 'DRINK', 'thom, beo nhe, tra sua', 0, 'trà gạo rang, sữa', 'sữa', 'tráng miệng', 'bánh flan', FALSE, 4

    UNION ALL SELECT 'Combo văn phòng A', 'Cơm bò lúc lắc, salad nhỏ và trà chanh cho 1 người', 119000, 'Combo', 'COMBO', 'van phong, no lau, tiet kiem', 1, 'cơm bò, salad, trà chanh', '', '1 người, ăn trưa', 'dùng nhanh buổi trưa', FALSE, 16
    UNION ALL SELECT 'Combo văn phòng B', 'Cơm gà teriyaki, súp cua và nước ép táo xanh', 129000, 'Combo', 'COMBO', 'khong cay, tre em, no vua', 0, 'cơm gà, súp cua, nước ép táo', 'trứng, hải sản', '1 người, không ăn cay', 'ăn trưa', FALSE, 16
    UNION ALL SELECT 'Combo healthy', 'Salad ức gà, cơm chay rau củ và nước ép cần tây táo', 159000, 'Combo', 'COMBO', 'healthy, it beo, thanh', 0, 'salad gà, cơm chay, nước ép', 'sữa, đậu nành', 'người ăn lành mạnh', 'ăn nhẹ buổi tối', FALSE, 16
    UNION ALL SELECT 'Combo trẻ em', 'Mì Ý bò bằm, phô mai que và sữa chua việt quất', 149000, 'Combo', 'COMBO', 'tre em, khong cay, de an', 0, 'mì Ý, phô mai que, sữa chua', 'sữa, gluten', 'trẻ em', 'gia đình', FALSE, 15
    UNION ALL SELECT 'Combo món Việt 1', 'Bún thịt nướng chả giò, gỏi ngó sen và trà tắc', 149000, 'Combo', 'COMBO', 'mon viet, chua ngot, tiet kiem', 1, 'bún thịt nướng, gỏi ngó sen, trà tắc', 'hải sản', '1-2 người', 'ăn trưa', FALSE, 14
    UNION ALL SELECT 'Combo món Việt 2', 'Cá kho tộ, rau củ luộc kho quẹt, cơm trắng và trà đá', 179000, 'Combo', 'COMBO', 'gia dinh, dam vi, truyen thong', 1, 'cá kho, rau củ, cơm, trà đá', 'cá', '2 người', 'bữa cơm Việt', FALSE, 18
    UNION ALL SELECT 'Combo hải sản nhẹ', 'Mực chiên giòn, cơm chiên hải sản và soda chanh dây', 189000, 'Combo', 'COMBO', 'hai san, gion, mat', 1, 'mực, cơm chiên hải sản, soda', 'hải sản, trứng', '2 người', 'ăn tối', FALSE, 18
    UNION ALL SELECT 'Combo gà giòn', 'Cánh gà chiên nước mắm, cơm gà xối mỡ và cola', 169000, 'Combo', 'COMBO', 'ga, gion, dam vi', 1, 'cánh gà, cơm gà, cola', '', '2 người', 'nhóm bạn', FALSE, 16
    UNION ALL SELECT 'Combo chay thanh nhẹ', 'Bún riêu chay, đậu hũ nướng sa tế và trà ô long vải', 159000, 'Combo', 'COMBO', 'chay, thanh, tiet kiem', 1, 'bún riêu chay, đậu hũ, trà ô long', 'đậu nành', 'ăn chay, 2 người', 'ăn tối nhẹ', TRUE, 16
    UNION ALL SELECT 'Combo bò đậm vị', 'Bò kho bánh mì, bò cuốn lá lốt và bia lager', 199000, 'Combo', 'COMBO', 'bo, dam vi, nhom ban', 1, 'bò kho, bò lá lốt, bia', 'gluten', '2 người lớn', 'ăn tối', FALSE, 18
    UNION ALL SELECT 'Combo lẩu đôi', 'Lẩu gà lá é nhỏ, há cảo tôm và 2 nước suối', 259000, 'Combo', 'COMBO', 'lau, am, 2 nguoi', 1, 'lẩu gà, há cảo, nước suối', 'hải sản, gluten', '2 người', 'ngày mưa', FALSE, 25
    UNION ALL SELECT 'Combo lẩu cay', 'Lẩu kim chi bò Mỹ, gỏi xoài tôm khô và 2 bia lager', 329000, 'Combo', 'COMBO', 'cay, lau, nhom ban', 3, 'lẩu kim chi, gỏi xoài, bia', 'hải sản, đậu phộng', '3-4 người thích cay', 'ăn tối', FALSE, 25
    UNION ALL SELECT 'Combo nướng gia đình', 'Sườn heo nướng, gà nướng muối ớt, salad và 4 nước ngọt', 429000, 'Combo', 'COMBO', 'nuong, gia dinh, no lau', 2, 'sườn, gà, salad, nước ngọt', '', '4 người', 'cuối tuần', FALSE, 24
    UNION ALL SELECT 'Combo nướng hải sản', 'Hải sản nướng phô mai, bạch tuộc sa tế và soda bạc hà', 359000, 'Combo', 'COMBO', 'hai san, nuong, beo', 2, 'hải sản, bạch tuộc, soda', 'hải sản, sữa', '2-3 người', 'nhóm bạn', FALSE, 22
    UNION ALL SELECT 'Combo ăn nhẹ', 'Khoai lang kén, phô mai que, trà dâu tằm và soda việt quất', 149000, 'Combo', 'COMBO', 'an vat, tre em, gion', 0, 'khoai lang, phô mai, trà, soda', 'sữa, gluten', '2 người, trẻ em', 'xế chiều', TRUE, 10
    UNION ALL SELECT 'Combo khai vị nhóm', 'Gỏi bò bóp thấu, tôm tempura, chạo tôm mía và trà chanh dây', 299000, 'Combo', 'COMBO', 'khai vi, nhom, da dang', 1, 'gỏi bò, tôm tempura, chạo tôm, trà', 'hải sản, gluten', '3-4 người', 'mở đầu bữa tiệc', FALSE, 16
    UNION ALL SELECT 'Combo cà phê sáng', 'Bò kho bánh mì, bạc xỉu và trái cây nhỏ', 119000, 'Combo', 'COMBO', 'sang, ca phe, no vua', 1, 'bò kho, bánh mì, bạc xỉu', 'sữa, gluten', '1 người', 'bữa sáng', FALSE, 14
    UNION ALL SELECT 'Combo không cay', 'Phở gà xé, salad cá ngừ và trà ô long vải', 149000, 'Combo', 'COMBO', 'khong cay, thanh, de an', 0, 'phở gà, salad cá ngừ, trà', 'cá, mè', 'người không ăn cay', 'ăn trưa', FALSE, 12
    UNION ALL SELECT 'Combo tiết kiệm 2 người', 'Bún bò xào sả, mì xào rau củ chay và 2 trà chanh', 169000, 'Combo', 'COMBO', 'tiet kiem, 2 nguoi, no vua', 1, 'bún bò, mì rau củ, trà chanh', 'gluten', '2 người', 'ăn nhanh', FALSE, 14
    UNION ALL SELECT 'Combo tiệc nhỏ', 'Lẩu cua đồng, nem nướng, cánh gà và mocktail nhiệt đới', 459000, 'Combo', 'COMBO', 'tiec nho, gia dinh, da dang', 1, 'lẩu cua, nem nướng, cánh gà, mocktail', 'hải sản, đậu phộng', '4-5 người', 'liên hoan', FALSE, 28

    UNION ALL SELECT 'Bò xiên nướng rau củ', 'Bò xiên nướng kèm ớt chuông, hành tây và sốt tiêu', 119000, 'Đồ nướng', 'GRILL', 'bo, thom, cay nhe', 2, 'bò, ớt chuông, hành tây, tiêu', '', 'nhóm bạn', 'bia lager', FALSE, 18
    UNION ALL SELECT 'Gà xiên nướng yakitori', 'Gà xiên nướng sốt yakitori ngọt mặn kiểu Nhật', 89000, 'Đồ nướng', 'GRILL', 'ngot man, ga, de an', 0, 'gà, sốt yakitori, mè', 'mè, đậu nành', 'trẻ em, nhóm nhỏ', 'trà sen vàng', FALSE, 15
    UNION ALL SELECT 'Heo xiên nướng sả', 'Heo xiên ướp sả nướng thơm, ăn cùng đồ chua', 85000, 'Đồ nướng', 'GRILL', 'thom sa, dam vi, heo', 1, 'thịt heo, sả, đồ chua', '', 'ăn nhẹ, nhậu nhẹ', 'bia lager', FALSE, 15
    UNION ALL SELECT 'Cá saba nướng muối', 'Cá saba nướng muối da giòn, dùng với chanh và củ cải', 119000, 'Đồ nướng', 'GRILL', 'ca, man nhe, thom', 0, 'cá saba, muối, chanh', 'cá', 'khách thích cá', 'trà chanh dây', FALSE, 18
    UNION ALL SELECT 'Cá lóc nướng trui', 'Cá lóc nướng trui cuốn rau sống, bún và bánh tráng', 189000, 'Đồ nướng', 'GRILL', 'dan da, ca, cuon', 1, 'cá lóc, rau sống, bún, bánh tráng', 'cá', 'nhóm 3-4 người', 'bia lager', FALSE, 25
    UNION ALL SELECT 'Tôm sú nướng muối ớt', 'Tôm sú nướng muối ớt cay thơm, thịt ngọt chắc', 169000, 'Đồ nướng', 'GRILL', 'hai san, cay, thom', 3, 'tôm sú, muối ớt', 'hải sản', 'người thích hải sản cay', 'nước suối', FALSE, 18
    UNION ALL SELECT 'Mực nướng sa tế', 'Mực nướng sa tế cay vừa, giòn ngọt và thơm khói', 145000, 'Đồ nướng', 'GRILL', 'hai san, cay vua, gion', 3, 'mực, sa tế, rau răm', 'hải sản', 'nhóm bạn', 'bia IPA', FALSE, 18
    UNION ALL SELECT 'Sò điệp nướng mỡ hành', 'Sò điệp nướng mỡ hành, đậu phộng và nước mắm chua ngọt', 139000, 'Đồ nướng', 'GRILL', 'hai san, beo, thom', 1, 'sò điệp, mỡ hành, đậu phộng', 'hải sản, đậu phộng', 'khai vị nướng', 'trà tắc', FALSE, 16
    UNION ALL SELECT 'Hàu nướng phô mai', 'Hàu nướng phô mai béo thơm, ăn nóng tại bàn', 159000, 'Đồ nướng', 'GRILL', 'hai san, beo, cao cap', 0, 'hàu, phô mai', 'hải sản, sữa', 'nhóm 2-3 người', 'soda chanh dây', FALSE, 16
    UNION ALL SELECT 'Nấm đùi gà nướng sốt mè', 'Nấm đùi gà nướng sốt mè thơm bùi, phù hợp ăn chay', 69000, 'Đồ nướng', 'GRILL', 'chay, nam, thom me', 0, 'nấm đùi gà, mè, sốt nấm', 'mè', 'ăn chay', 'trà ô long vải', TRUE, 12
    UNION ALL SELECT 'Bắp nướng mỡ hành', 'Bắp Mỹ nướng mỡ hành, vị ngọt bùi và thơm khói', 45000, 'Đồ nướng', 'GRILL', 'chay, ngot, thom', 0, 'bắp, mỡ hành', '', 'ăn nhẹ, trẻ em', 'trà dâu tằm', TRUE, 10
    UNION ALL SELECT 'Khoai tây nướng phô mai', 'Khoai tây nướng phủ phô mai béo thơm', 59000, 'Đồ nướng', 'GRILL', 'beo, chay, tre em', 0, 'khoai tây, phô mai', 'sữa', 'trẻ em, ăn nhẹ', 'sữa chua việt quất', TRUE, 12
    UNION ALL SELECT 'Ba chỉ heo nướng kim chi', 'Ba chỉ heo nướng ăn kèm kim chi, rau sống và sốt cay', 139000, 'Đồ nướng', 'GRILL', 'beo, cay, han quoc', 3, 'ba chỉ heo, kim chi, rau sống', '', 'người thích cay', 'bia lager', FALSE, 18
    UNION ALL SELECT 'Dẻ sườn bò nướng BBQ', 'Dẻ sườn bò nướng sốt BBQ ngọt khói', 189000, 'Đồ nướng', 'GRILL', 'bo, bbq, ngot khoi', 1, 'dẻ sườn bò, sốt BBQ', '', 'nhóm 2-3 người', 'bia thủ công IPA', FALSE, 22
    UNION ALL SELECT 'Xúc xích Đức nướng', 'Xúc xích Đức nướng dùng cùng mù tạt và khoai tây', 99000, 'Đồ nướng', 'GRILL', 'dam vi, tay, de an', 0, 'xúc xích, mù tạt, khoai tây', '', 'nhóm bạn, trẻ em', 'cola', FALSE, 12
    UNION ALL SELECT 'Cánh gà nướng mật ong', 'Cánh gà nướng mật ong vàng thơm, cay rất nhẹ', 92000, 'Đồ nướng', 'GRILL', 'ngot, ga, thom', 1, 'cánh gà, mật ong', '', 'trẻ em, nhóm bạn', 'trà đào', FALSE, 18
    UNION ALL SELECT 'Đùi cừu nướng rosemary', 'Đùi cừu nướng rosemary, sốt tiêu đen và rau củ', 249000, 'Đồ nướng', 'GRILL', 'cao cap, thom thao moc, dam', 1, 'cừu, rosemary, tiêu đen, rau củ', '', 'khách thích món Âu', 'rượu vang demo, soda', FALSE, 28
    UNION ALL SELECT 'Sườn bò nướng phô mai', 'Sườn bò nướng phủ phô mai kéo sợi béo thơm', 219000, 'Đồ nướng', 'GRILL', 'bo, beo, phomai', 1, 'sườn bò, phô mai', 'sữa', 'nhóm 2-3 người', 'soda việt quất', FALSE, 24
    UNION ALL SELECT 'Rau củ nướng thảo mộc', 'Rau củ nướng dầu olive và thảo mộc, vị thanh nhẹ', 69000, 'Đồ nướng', 'GRILL', 'chay, healthy, thao moc', 0, 'bí ngòi, cà tím, ớt chuông, dầu olive', '', 'ăn chay, healthy', 'nước ép cần tây táo', TRUE, 14
    UNION ALL SELECT 'Đậu bắp nướng muối ớt', 'Đậu bắp nướng muối ớt cay nhẹ, giòn ngọt', 49000, 'Đồ nướng', 'GRILL', 'chay, cay nhe, gion', 2, 'đậu bắp, muối ớt', '', 'ăn nhẹ, ăn chay', 'trà chanh', TRUE, 10

    UNION ALL SELECT 'Lẩu Thái tomyum', 'Lẩu Thái tomyum chua cay với tôm, mực, nghêu và nấm', 269000, 'Lẩu', 'HOTPOT', 'chua cay, hai san, thai', 4, 'tôm, mực, nghêu, nấm, lá chanh', 'hải sản', 'nhóm 3-4 người thích cay', 'nước suối, bia lager', FALSE, 28
    UNION ALL SELECT 'Lẩu hải sản chua cay', 'Lẩu hải sản nước dùng chua cay đậm vị, nhiều topping', 289000, 'Lẩu', 'HOTPOT', 'hai san, chua cay, dam', 3, 'tôm, mực, cá, nghêu, rau', 'hải sản', 'nhóm 4 người', 'trà chanh dây', FALSE, 28
    UNION ALL SELECT 'Lẩu cá kèo lá giang', 'Lẩu cá kèo lá giang vị chua thanh miền Tây', 239000, 'Lẩu', 'HOTPOT', 'chua thanh, ca, mien tay', 1, 'cá kèo, lá giang, rau, bún', 'cá', 'gia đình, nhóm bạn', 'trà tắc', FALSE, 25
    UNION ALL SELECT 'Lẩu riêu cua bắp bò', 'Lẩu riêu cua bắp bò, nước dùng chua dịu và thơm riêu', 279000, 'Lẩu', 'HOTPOT', 'rieu cua, bo, chua diu', 1, 'riêu cua, bắp bò, đậu hũ, rau', 'hải sản, đậu nành', 'gia đình', 'nước ép cam', FALSE, 28
    UNION ALL SELECT 'Lẩu vịt nấu chao', 'Lẩu vịt nấu chao béo thơm, ăn kèm rau muống và bún', 259000, 'Lẩu', 'HOTPOT', 'beo, dam, vit', 1, 'vịt, chao, khoai môn, rau muống', 'đậu nành', 'nhóm 3-4 người', 'bia lager', FALSE, 30
    UNION ALL SELECT 'Lẩu dê thuốc bắc', 'Lẩu dê thuốc bắc ấm bụng với rau xanh và mì', 299000, 'Lẩu', 'HOTPOT', 'am, thao moc, de', 1, 'dê, thuốc bắc, rau, mì', 'gluten', 'người thích vị thuốc bắc', 'trà gừng', FALSE, 30
    UNION ALL SELECT 'Lẩu sườn sụn om sấu', 'Lẩu sườn sụn om sấu chua dịu, ăn kèm bún và rau', 249000, 'Lẩu', 'HOTPOT', 'chua diu, suon, thanh', 1, 'sườn sụn, sấu, rau, bún', '', 'gia đình', 'trà đào', FALSE, 26
    UNION ALL SELECT 'Lẩu bò sa tế', 'Lẩu bò sa tế cay thơm với bò Mỹ, nấm và rau xanh', 269000, 'Lẩu', 'HOTPOT', 'bo, cay, sate', 4, 'bò Mỹ, sa tế, nấm, rau', '', 'người thích cay', 'bia IPA', FALSE, 26
    UNION ALL SELECT 'Lẩu gà nấm', 'Lẩu gà nấm ngọt thanh với nấm hương, nấm kim châm', 229000, 'Lẩu', 'HOTPOT', 'ga, nam, thanh', 0, 'gà, nấm, rau, bún', '', 'trẻ em, gia đình', 'nước ép táo xanh', FALSE, 25
    UNION ALL SELECT 'Lẩu gà ớt hiểm', 'Lẩu gà ớt hiểm cay thơm, nước dùng đậm và ấm', 249000, 'Lẩu', 'HOTPOT', 'ga, cay, am', 4, 'gà, ớt hiểm, rau, mì', '', 'người thích cay', 'nước suối', FALSE, 25
    UNION ALL SELECT 'Lẩu cá hồi măng chua', 'Lẩu cá hồi măng chua vị thanh, cá béo nhẹ', 289000, 'Lẩu', 'HOTPOT', 'ca, chua thanh, beo nhe', 1, 'cá hồi, măng chua, rau, bún', 'cá', 'khách thích cá', 'trà ô long vải', FALSE, 26
    UNION ALL SELECT 'Lẩu tôm càng', 'Lẩu tôm càng nước dùng ngọt thanh, rau và nấm', 329000, 'Lẩu', 'HOTPOT', 'hai san, tom, cao cap', 1, 'tôm càng, nấm, rau, bún', 'hải sản', 'nhóm 3-4 người', 'soda chanh dây', FALSE, 28
    UNION ALL SELECT 'Lẩu chay thảo mộc', 'Lẩu chay thảo mộc với nấm, rau củ và đậu hũ', 199000, 'Lẩu', 'HOTPOT', 'chay, thao moc, thanh', 0, 'nấm, rau củ, đậu hũ, thảo mộc', 'đậu nành', 'ăn chay, healthy', 'trà sen vàng', TRUE, 22
    UNION ALL SELECT 'Lẩu miso chay', 'Lẩu miso chay với rong biển, đậu hũ và nấm', 209000, 'Lẩu', 'HOTPOT', 'chay, miso, nhat', 0, 'miso, rong biển, đậu hũ, nấm', 'đậu nành', 'ăn chay, khách thích Nhật', 'trà gạo rang sữa', TRUE, 22
    UNION ALL SELECT 'Lẩu collagen gà', 'Lẩu collagen gà nước dùng béo nhẹ, nấm và rau xanh', 279000, 'Lẩu', 'HOTPOT', 'ga, beo nhe, bo duong', 0, 'gà, collagen, nấm, rau', '', 'gia đình, khách thích vị nhẹ', 'nước ép cà rốt cam', FALSE, 28
    UNION ALL SELECT 'Lẩu tiêu xanh bò viên', 'Lẩu tiêu xanh với bò viên, nấm và rau cải', 239000, 'Lẩu', 'HOTPOT', 'tieu xanh, bo, am', 2, 'tiêu xanh, bò viên, nấm, rau', '', 'ngày mưa, ăn tối', 'trà gừng', FALSE, 25
    UNION ALL SELECT 'Lẩu mắm miền Tây', 'Lẩu mắm đậm vị miền Tây với cá, tôm, thịt và rau đồng', 299000, 'Lẩu', 'HOTPOT', 'dam vi, mien tay, hai san', 2, 'mắm, cá, tôm, thịt, rau đồng', 'hải sản, cá', 'người thích vị đậm', 'nước chanh sả', FALSE, 30
    UNION ALL SELECT 'Lẩu ếch măng cay', 'Lẩu ếch măng cay thơm, thịt ếch săn và nước dùng đậm', 259000, 'Lẩu', 'HOTPOT', 'ech, cay, mang', 4, 'ếch, măng, sa tế, rau', '', 'người thích cay', 'bia lager', FALSE, 26
    UNION ALL SELECT 'Lẩu bò nhúng mẻ', 'Lẩu bò nhúng mẻ chua dịu, ăn cùng rau sống và bún', 249000, 'Lẩu', 'HOTPOT', 'bo, chua diu, thanh', 1, 'bò, mẻ, rau sống, bún', '', 'nhóm 2-3 người', 'trà tắc', FALSE, 25
    UNION ALL SELECT 'Lẩu tokbokki phô mai', 'Lẩu tokbokki phô mai cay nhẹ, chả cá và xúc xích', 219000, 'Lẩu', 'HOTPOT', 'han quoc, beo, cay nhe', 2, 'tokbokki, phô mai, chả cá, xúc xích', 'sữa, cá', 'trẻ em lớn, nhóm bạn', 'cola', FALSE, 20

    UNION ALL SELECT 'Bia không cồn', 'Bia không cồn 330ml ướp lạnh, phù hợp khách lái xe', 30000, 'Bia/nước giải khát', 'DRINK', 'khong con, mat, nhe', 0, 'bia không cồn', '', 'người lớn, khách lái xe', 'đồ nướng', TRUE, 1
    UNION ALL SELECT 'Bia stout đen', 'Bia stout đen vị rang nhẹ, hậu đắng êm', 70000, 'Bia/nước giải khát', 'DRINK', 'dang nhe, rang, dam', 0, 'bia stout', '', 'người lớn thích bia đậm', 'sườn bò nướng', FALSE, 1
    UNION ALL SELECT 'Bia wheat', 'Bia wheat thơm lúa mì, vị nhẹ dễ uống', 62000, 'Bia/nước giải khát', 'DRINK', 'nhe, lua mi, mat', 0, 'bia wheat', 'gluten', 'người lớn', 'hải sản nướng', FALSE, 1
    UNION ALL SELECT 'Bia trái cây đào', 'Bia trái cây vị đào, nhẹ và thơm', 58000, 'Bia/nước giải khát', 'DRINK', 'trai cay, dao, nhe', 0, 'bia, đào', 'gluten', 'người lớn thích vị ngọt', 'món chiên', FALSE, 1
    UNION ALL SELECT 'Bia trái cây chanh dây', 'Bia trái cây chanh dây chua nhẹ, mát lạnh', 58000, 'Bia/nước giải khát', 'DRINK', 'trai cay, chua nhe, mat', 0, 'bia, chanh dây', 'gluten', 'người lớn', 'lẩu cay', FALSE, 1
    UNION ALL SELECT 'Nước ngọt cam', 'Nước ngọt vị cam lon 330ml ướp lạnh', 22000, 'Bia/nước giải khát', 'DRINK', 'ngot, co gas, cam', 0, 'nước ngọt cam', '', 'trẻ em, nhóm bạn', 'combo', TRUE, 1
    UNION ALL SELECT 'Nước ngọt chanh', 'Nước ngọt vị chanh có gas, mát lạnh', 22000, 'Bia/nước giải khát', 'DRINK', 'chua nhe, co gas, mat', 0, 'nước ngọt chanh', '', 'giải khát', 'món nướng', TRUE, 1
    UNION ALL SELECT 'Nước tăng lực', 'Nước tăng lực lon 250ml, vị ngọt mạnh', 25000, 'Bia/nước giải khát', 'DRINK', 'ngot, manh, tinh tao', 0, 'nước tăng lực', '', 'người lớn', 'món cay', TRUE, 1
    UNION ALL SELECT 'Nước khoáng có gas', 'Nước khoáng có gas giúp giải ngấy, không đường', 28000, 'Bia/nước giải khát', 'DRINK', 'co gas, khong duong, thanh', 0, 'nước khoáng có gas', '', 'khách healthy', 'món béo', TRUE, 1
    UNION ALL SELECT 'Trà xanh đóng chai', 'Trà xanh đóng chai ướp lạnh, vị thanh nhẹ', 20000, 'Bia/nước giải khát', 'DRINK', 'tra xanh, thanh, mat', 0, 'trà xanh', '', 'mọi khách hàng', 'cơm, bún', TRUE, 1
    UNION ALL SELECT 'Trà đào đóng chai', 'Trà đào đóng chai mát lạnh, vị ngọt thơm', 24000, 'Bia/nước giải khát', 'DRINK', 'dao, ngot, mat', 0, 'trà đào', '', 'trẻ em, nhóm bạn', 'đồ chiên', TRUE, 1
    UNION ALL SELECT 'Sữa bắp chai', 'Sữa bắp chai vị bùi ngọt, dùng lạnh', 28000, 'Bia/nước giải khát', 'DRINK', 'sua bap, ngot, bùi', 0, 'bắp, sữa', 'sữa', 'trẻ em, tráng miệng', 'món không cay', FALSE, 1
    UNION ALL SELECT 'Sữa đậu nành chai', 'Sữa đậu nành chai thơm nhẹ, ít ngọt', 22000, 'Bia/nước giải khát', 'DRINK', 'dau nanh, it ngot, thanh', 0, 'đậu nành', 'đậu nành', 'ăn chay, healthy', 'bún riêu chay', TRUE, 1
    UNION ALL SELECT 'Nước nha đam', 'Nước nha đam đóng chai, mát và thanh', 26000, 'Bia/nước giải khát', 'DRINK', 'mat, nha dam, thanh', 0, 'nha đam', '', 'giải khát', 'lẩu cay', TRUE, 1
    UNION ALL SELECT 'Nước yến lon', 'Nước yến lon ướp lạnh, vị ngọt nhẹ', 35000, 'Bia/nước giải khát', 'DRINK', 'bo duong, ngot nhe, mat', 0, 'nước yến', '', 'người lớn, gia đình', 'món Việt', FALSE, 1
    UNION ALL SELECT 'Nước dừa đóng chai', 'Nước dừa đóng chai mát lạnh, vị ngọt tự nhiên', 30000, 'Bia/nước giải khát', 'DRINK', 'dua, mat, tu nhien', 0, 'nước dừa', '', 'mọi khách hàng', 'đồ nướng cay', TRUE, 1
    UNION ALL SELECT 'Nước sâm bí đao', 'Nước sâm bí đao thanh mát, ít ngọt', 24000, 'Bia/nước giải khát', 'DRINK', 'thanh mat, thao moc, it ngot', 0, 'bí đao, thảo mộc', '', 'giải nhiệt', 'món cay', TRUE, 1
    UNION ALL SELECT 'Kombucha gừng chanh', 'Kombucha gừng chanh chua nhẹ, có gas tự nhiên', 52000, 'Bia/nước giải khát', 'DRINK', 'healthy, chua nhe, co gas', 0, 'kombucha, gừng, chanh', '', 'khách healthy', 'salad', TRUE, 1
    UNION ALL SELECT 'Kombucha dâu hibiscus', 'Kombucha dâu hibiscus màu đỏ đẹp, vị chua ngọt', 54000, 'Bia/nước giải khát', 'DRINK', 'healthy, dau, chua ngot', 0, 'kombucha, dâu, hibiscus', '', 'khách healthy', 'món nhẹ', TRUE, 1
    UNION ALL SELECT 'Nước điện giải', 'Nước điện giải đóng chai, bù khoáng và ít ngọt', 26000, 'Bia/nước giải khát', 'DRINK', 'dien giai, it ngot, mat', 0, 'nước điện giải', '', 'mọi khách hàng', 'món cay, lẩu', TRUE, 1
) s
JOIN categories c ON c.name = s.category_name
WHERE NOT EXISTS (
    SELECT 1 FROM menu_items m WHERE LOWER(TRIM(m.name)) = LOWER(TRIM(s.name))
);
