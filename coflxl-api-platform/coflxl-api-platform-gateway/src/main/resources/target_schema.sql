CREATE TABLE IF NOT EXISTS target_user (
    id BIGINT PRIMARY KEY,
    name VARCHAR(64),
    age INT
);
TRUNCATE TABLE target_user;
INSERT INTO target_user (id, name, age) VALUES (1, 'Alice', 25);
INSERT INTO target_user (id, name, age) VALUES (2, 'Bob', 30);
INSERT INTO target_user (id, name, age) VALUES (3, 'Charlie', 35);

CREATE TABLE IF NOT EXISTS target_order (
    id BIGINT PRIMARY KEY,
    order_no VARCHAR(64),
    total_amount DECIMAL(10, 2)
);
TRUNCATE TABLE target_order;

CREATE TABLE IF NOT EXISTS target_order_item (
    id BIGINT PRIMARY KEY,
    order_id BIGINT,
    item_name VARCHAR(128),
    price DECIMAL(10, 2)
);
TRUNCATE TABLE target_order_item;
