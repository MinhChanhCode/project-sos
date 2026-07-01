ALTER TABLE menu_items
    ADD COLUMN type VARCHAR(80) NULL,
    ADD COLUMN taste_tags VARCHAR(500) NULL,
    ADD COLUMN spicy_level INT NULL,
    ADD COLUMN ingredients VARCHAR(800) NULL,
    ADD COLUMN allergens VARCHAR(500) NULL,
    ADD COLUMN suitable_for VARCHAR(500) NULL,
    ADD COLUMN pairing VARCHAR(500) NULL,
    ADD COLUMN is_vegetarian BOOLEAN NULL,
    ADD COLUMN prep_time_minutes INT NULL;

UPDATE menu_items
SET type = CASE
        WHEN LOWER(name) LIKE '%trà%' OR LOWER(name) LIKE '%cà phê%' OR LOWER(name) LIKE '%sinh tố%' OR LOWER(name) LIKE '%nước%' OR LOWER(name) LIKE '%latte%' THEN 'DRINK'
        WHEN LOWER(name) LIKE '%combo%' THEN 'COMBO'
        WHEN LOWER(name) LIKE '%chè%' OR LOWER(name) LIKE '%kem%' OR LOWER(name) LIKE '%bánh flan%' OR LOWER(name) LIKE '%su kem%' THEN 'DESSERT'
        WHEN LOWER(name) LIKE '%gỏi%' OR LOWER(name) LIKE '%chả giò%' OR LOWER(name) LIKE '%khoai%' OR LOWER(name) LIKE '%salad%' THEN 'APPETIZER'
        ELSE 'MAIN'
    END,
    spicy_level = CASE
        WHEN LOWER(description) LIKE '%chua cay%' OR LOWER(name) LIKE '%lẩu thái%' THEN 3
        WHEN LOWER(description) LIKE '%hơi cay%' OR LOWER(name) LIKE '%bún bò%' THEN 2
        WHEN LOWER(description) LIKE '%không cay%' THEN 0
        ELSE 1
    END,
    is_vegetarian = CASE
        WHEN LOWER(name) LIKE '%trà%' OR LOWER(name) LIKE '%cà phê%' OR LOWER(name) LIKE '%sinh tố%' OR LOWER(name) LIKE '%nước%' OR LOWER(name) LIKE '%chè%' OR LOWER(name) LIKE '%kem%' THEN TRUE
        ELSE FALSE
    END
WHERE type IS NULL;

UPDATE menu_items
SET taste_tags = TRIM(BOTH ', ' FROM CONCAT(
        CASE WHEN LOWER(description) LIKE '%ngọt%' OR LOWER(name) LIKE '%trà sữa%' OR LOWER(name) LIKE '%chè%' THEN 'ngọt, ' ELSE '' END,
        CASE WHEN LOWER(description) LIKE '%béo%' OR LOWER(name) LIKE '%latte%' OR LOWER(name) LIKE '%bơ%' THEN 'béo, ' ELSE '' END,
        CASE WHEN LOWER(description) LIKE '%chua%' OR LOWER(name) LIKE '%trà tắc%' OR LOWER(name) LIKE '%lẩu thái%' THEN 'chua, ' ELSE '' END,
        CASE WHEN LOWER(description) LIKE '%cay%' OR LOWER(name) LIKE '%bún bò%' THEN 'cay, ' ELSE '' END,
        CASE WHEN LOWER(description) LIKE '%giòn%' OR LOWER(name) LIKE '%chả giò%' OR LOWER(name) LIKE '%khoai%' THEN 'giòn, ' ELSE '' END,
        CASE WHEN LOWER(description) LIKE '%thanh%' OR LOWER(description) LIKE '%nhẹ%' THEN 'thanh nhẹ, ' ELSE '' END
    )),
    suitable_for = CASE
        WHEN type = 'COMBO' THEN 'đi nhóm, tiết kiệm thời gian chọn món'
        WHEN type = 'DRINK' THEN 'uống kèm món chính, giải khát'
        WHEN type = 'APPETIZER' THEN 'khai vị, ăn nhẹ, chia sẻ theo nhóm'
        WHEN spicy_level >= 2 THEN 'khách thích vị đậm và cay'
        ELSE 'bữa chính cá nhân'
    END,
    pairing = CASE
        WHEN type = 'MAIN' AND spicy_level >= 2 THEN 'trà tắc xí muội, nước ép cam'
        WHEN type = 'MAIN' THEN 'trà tắc xí muội, nước ép cam, gỏi cuốn tôm thịt'
        WHEN type = 'APPETIZER' THEN 'cơm gà xối mỡ, phở bò tái, trà tắc xí muội'
        WHEN type = 'DRINK' THEN 'phở bò tái, cơm tấm sườn bì chả, bánh mì chảo'
        ELSE 'món chính và đồ uống theo khẩu vị'
    END,
    allergens = CASE
        WHEN LOWER(name) LIKE '%hải sản%' OR LOWER(description) LIKE '%tôm%' OR LOWER(description) LIKE '%mực%' THEN 'hải sản'
        WHEN LOWER(description) LIKE '%sữa%' OR LOWER(name) LIKE '%latte%' OR LOWER(name) LIKE '%trà sữa%' THEN 'sữa'
        WHEN LOWER(description) LIKE '%đậu phộng%' THEN 'đậu phộng'
        ELSE NULL
    END,
    prep_time_minutes = CASE
        WHEN type = 'DRINK' THEN 5
        WHEN type = 'APPETIZER' THEN 10
        WHEN type = 'COMBO' THEN 20
        WHEN LOWER(name) LIKE '%lẩu%' THEN 25
        ELSE 15
    END
WHERE taste_tags IS NULL OR suitable_for IS NULL OR pairing IS NULL;
