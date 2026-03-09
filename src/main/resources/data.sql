INSERT INTO P_STORE(
id,
owner_id,
region_id,
category,
name,
road_address,
detail_address,
status,
latitude,
longitude,
created_at,
updated_at,
created_by,
updated_by,
is_deleted) VALUES
(
    '7f1c6e6a-8c12-4c9a-9c9c-3c2b0d7f1234',
    '1a3b5d7f-9e11-4a6c-bc1f-2d9a0e6f5678',
    '5c8a2b7e-3a1d-4b7f-9e1a-3c5d7e9f0001',
    'CHICKEN',
    '홍대맛있는치킨',
    '서울특별시 마포구 와우산로 94',
    '2층 201호',
    'OPENED',
    37.551234,
    126.922345,
    NOW(),
    NOW(),
    '38075506-237e-44c3-8195-035f561ae294',
    '38075506-237e-44c3-8195-035f561ae294',
    false
);

INSERT INTO P_PRODUCT(
id,
name,
description,
price,
status,
store_id,
created_at,
updated_at,
created_by,
updated_by,
is_deleted) VALUES
(
    'adc58080-dbcc-4607-bb37-72b8d4ecfe0a',
    'test_product',
    'test_description',
    100.0,
    'ON_SALE',
    '7f1c6e6a-8c12-4c9a-9c9c-3c2b0d7f1234',
    NOW(),
    NOW(),
    '38075506-237e-44c3-8195-035f561ae294',
    '38075506-237e-44c3-8195-035f561ae294',
    false
);