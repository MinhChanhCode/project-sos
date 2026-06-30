USE sos;

-- ĐỒ UỐNG (category_id = 1)
INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
VALUES
    ('Nước ép dứa', 'Dứa tươi ép lạnh, không đường', 32000,
     'https://images.unsplash.com/photo-1601004890684-d8cbf643f5f2?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Nước ép ổi', 'Ổi hồng ép nguyên chất', 30000,
     'https://cdn.pixabay.com/photo/2017/01/20/15/06/drink-1995054_1280.jpg',
     1, TRUE, TRUE),
    ('Sữa tươi trân châu đường đen', 'Sữa tươi thanh mát kèm trân châu đường đen', 45000,
     'https://images.unsplash.com/photo-1582384548479-90843579c2e2?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Trà vải', 'Trà đen kết hợp vải thiều tươi', 39000,
     'https://images.unsplash.com/photo-1528229247009-5d1ec036b730?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Cà phê đen đá', 'Cà phê phin truyền thống Việt Nam', 25000,
     'https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Trà gừng mật ong', 'Trà nóng kết hợp gừng và mật ong', 27000,
     'https://images.unsplash.com/photo-1526401485004-8b639c30e8c9?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Soda bạc hà', 'Soda mát lạnh pha hương bạc hà', 33000,
     'https://images.unsplash.com/photo-1570968915860-5305f2f0c0ff?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Trà chanh', 'Trà xanh pha chanh tươi', 25000,
     'https://images.unsplash.com/photo-1571771688431-5c0b3893dfbe?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Nước dừa tươi', 'Nước dừa nguyên chất, mát lạnh', 28000,
     'https://images.unsplash.com/photo-1506354666786-959d6d497f1a?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Sữa đậu nành', 'Sữa đậu nành nguyên chất nấu tại chỗ', 20000,
     'https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Trà đào hạt chia', 'Trà đào mát lạnh kết hợp hạt chia bổ dưỡng', 37000,
     'https://images.unsplash.com/photo-1601004890684-d8cbf643f5f2?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Cacao nóng', 'Ca cao nguyên chất pha sữa nóng', 29000,
     'https://images.unsplash.com/photo-1543353071-087092ec3933?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Trà sen nhãn nhục', 'Trà thảo mộc mát gan, ngủ ngon', 35000,
     'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Yaourt đá', 'Sữa chua truyền thống với đá bào', 18000,
     'https://images.unsplash.com/photo-1587732136601-067a1d36327d?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE),
    ('Trà hoa cúc mật ong', 'Trà hoa cúc sấy khô pha cùng mật ong', 32000,
     'https://images.unsplash.com/photo-1524594157363-6260063ac272?auto=format&fit=crop&w=500&q=60',
     1, TRUE, TRUE);

-- Seed promotions for some items (original_price, promotional_price, promotion_end_date)
-- Chọn một vài món làm khuyến mãi trong 3 ngày kể từ khi seed
UPDATE menu_items
SET original_price = price,
    promotional_price = price * 0.85,
    promotion_end_date = DATE_ADD(NOW(), INTERVAL 3 DAY)
WHERE name IN ('Nước ép dứa', 'Sữa tươi trân châu đường đen', 'Trà đào hạt chia');

-- MÓN CHÍNH (category_id = 2)
INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
VALUES
    ('Bún chả Hà Nội', 'Bún ăn kèm chả nướng và nước mắm pha', 55000,
     'https://images.unsplash.com/photo-1562967916-eb82221dfb40?auto=format&fit=crop&w=500&q=60',
     2, TRUE, TRUE),
    ('Bánh cuốn nóng', 'Bánh cuốn nhân thịt băm ăn kèm chả quế', 40000,
     'https://images.unsplash.com/photo-1585125500350-cf17fa9f70f8?auto=format&fit=crop&w=500&q=60',
     2, TRUE, TRUE),
    -- (Ồn dữ quá, bạn có thể tiếp tục tương tự theo mẫu trên)
    ('Bún riêu cua', 'Bún riêu nấu từ cua đồng, đậu hũ, cà chua', 48000,
     'https://images.unsplash.com/photo-1587300003388-59208cc962cb?auto=format&fit=crop&w=500&q=60',
     2, TRUE, TRUE),
    ('Cơm sườn nướng mật ong', 'Sườn mềm, ướp đậm đà, nướng thơm', 58000,
     'https://images.unsplash.com/photo-1584270354949-3dc5399c03f0?auto=format&fit=crop&w=500&q=60',
     2, TRUE, TRUE);

-- Khuyến mãi cho món chính trong 2 ngày
UPDATE menu_items
SET original_price = price,
    promotional_price = price * 0.9,
    promotion_end_date = DATE_ADD(NOW(), INTERVAL 2 DAY)
WHERE name IN ('Bún chả Hà Nội');

-- TRÁNG MIỆNG (category_id = 3)
INSERT INTO menu_items (name, description, price, image_url, category_id, is_available, is_active)
VALUES
    ('Bánh su kem', 'Bánh su kem nhân vanilla mềm mịn', 25000,
     'https://images.unsplash.com/photo-1599785209707-9bdb5279ca67?auto=format&fit=crop&w=500&q=60',
     3, TRUE, TRUE),
    ('Chè thập cẩm', 'Chè đủ loại đậu, thạch, nước cốt dừa', 28000,
     'https://images.unsplash.com/photo-1601597105550-5fde648fc3bb?auto=format&fit=crop&w=500&q=60',
     3, TRUE, TRUE),
    ('Kem sầu riêng', 'Kem thơm đậm mùi sầu riêng tươi', 37000,
     'https://images.unsplash.com/photo-1608747679354-1e9866a0711b?auto=format&fit=crop&w=500&q=60',
     3, TRUE, TRUE);

-- Khuyến mãi cho tráng miệng trong 1 ngày
UPDATE menu_items
SET original_price = price,
    promotional_price = price * 0.8,
    promotion_end_date = DATE_ADD(NOW(), INTERVAL 1 DAY)
WHERE name IN ('Bánh su kem');
INSERT INTO tables (id, name, capacity, is_available) VALUES
                                                          (UUID_TO_BIN(UUID()), 'Bàn 1', 4, TRUE),
                                                          (UUID_TO_BIN(UUID()), 'Bàn 2', 6, TRUE),
                                                          (UUID_TO_BIN(UUID()), 'Bàn 3', 2, TRUE);