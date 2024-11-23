-- Create the database
CREATE DATABASE "order-service";

-- Switch to the new database
\c "order-service";

-- Create orders table
CREATE TABLE t_orders (
                          id BIGSERIAL PRIMARY KEY,
                          order_number VARCHAR(255) NOT NULL
);

-- Create order line items table
CREATE TABLE order_line_items (
                                  id BIGSERIAL PRIMARY KEY,
                                  sku_code VARCHAR(255),
                                  price NUMERIC,
                                  quantity INTEGER,
                                  order_id BIGINT,
                                  CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES t_orders(id)
);
