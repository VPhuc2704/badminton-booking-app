-- 0. ENUM types
CREATE TYPE user_role AS ENUM ('CUSTOMER', 'ADMIN');
CREATE TYPE court_status AS ENUM ('AVAILABLE', 'MAINTENANCE', 'UNAVAILABLE');
CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED');
CREATE TYPE court_type AS ENUM ('INDOOR', 'OUTDOOR');


-- 1. Users
CREATE TABLE users (
                       id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100),
                       role_name user_role NOT NULL DEFAULT 'CUSTOMER',
                       is_active BOOLEAN DEFAULT TRUE,
                       create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Customers (1-1 với Users)
CREATE TABLE customers (
                       id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       user_id INT NOT NULL UNIQUE,
                       phone VARCHAR(20),
                       create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Courts
CREATE TABLE courts (
                        id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        court_name VARCHAR(50) NOT NULL,
                        court_type court_type NOT NULL,
                        status court_status DEFAULT 'AVAILABLE',
                        hourly_rate DECIMAL(10,2) NOT NULL,
                        description TEXT,
                        images JSON,
                        is_active BOOLEAN DEFAULT TRUE,
                        create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Bookings
CREATE TABLE bookings (
                          id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          booking_code VARCHAR(20) UNIQUE,
                          customer_id INT NOT NULL,
                          court_id INT NOT NULL,
                          booking_date DATE NOT NULL,
                          start_time TIME NOT NULL,
                          end_time TIME NOT NULL,
                          booking_status booking_status DEFAULT 'PENDING',
                          total_amount DECIMAL(10,2),
                          create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          payment_status VARCHAR(20) DEFAULT 'UNPAID',
                          FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
                          FOREIGN KEY (court_id) REFERENCES courts(id)
);

-- 5. Services
CREATE TABLE services (
                          id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          service_name VARCHAR(100) NOT NULL,
                          service_type VARCHAR(50),
                          description TEXT,
                          unit_price DECIMAL(10,2) NOT NULL,
                          create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 8. Transactions
CREATE TABLE transactions (
                          id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          amount DECIMAL(10,2) NOT NULL,
                          transaction_date DATE DEFAULT CURRENT_DATE,
                          payment_method VARCHAR(50),
                          booking_id INT,
                          created_by INT,
                          create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 9. AI
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE document_chunks (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         chunk_text TEXT NOT NULL,
                         metadata JSONB,
                         embedding vector(768),
                         create_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX ON document_chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);


INSERT INTO users (email, password_hash, full_name, role_name, is_active, create_at) VALUES
     ('admin@gmail.com','$2a$10$H72E4wEjol.znu0xwxtnJ.TXYCbwGzu3IugzONz3XAf.E4JE3BjCu','Admin User','ADMIN', true,'2025-08-20 09:00:00'),
     ('vannguyen@gmail.com','$2a$10$H72E4wEjol.znu0xwxtnJ.TXYCbwGzu3IugzONz3XAf.E4JE3BjCu','Nguyễn Khánh Vân','CUSTOMER', true,'2025-09-01 09:00:00'),
     ('dotans@gmail.com','$2a$10$H72E4wEjol.znu0xwxtnJ.TXYCbwGzu3IugzONz3XAf.E4JE3BjCu',' Đỗ Văn Tấn','CUSTOMER', true,'2025-09-02 09:00:00'),
     ('duykhanh@gmail.com','$2a$10$H72E4wEjol.znu0xwxtnJ.TXYCbwGzu3IugzONz3XAf.E4JE3BjCu','Lê Duy Khanh','CUSTOMER', true,'2025-09-03 09:00:00'),
     ('alice@gmail.com','$2a$10$H72E4wEjol.znu0xwxtnJ.TXYCbwGzu3IugzONz3XAf.E4JE3BjCu','Alice Nguyễn','CUSTOMER', true,'2025-09-04 09:00:00'),
     ('tranchien@gmail.com','$2a$10$H72E4wEjol.znu0xwxtnJ.TXYCbwGzu3IugzONz3XAf.E4JE3BjCu','Trần Minh Chiến','CUSTOMER', true,'2025-09-05 09:00:00'),
     ('hoangviet@gmail.com','$2a$10$H72E4wEjol.znu0xwxtnJ.TXYCbwGzu3IugzONz3XAf.E4JE3BjCu','Đặng Hoàng Việt','CUSTOMER', true,'2025-09-06 09:00:00'),
     ('dainghia@gmail.com','$2a$10$H72E4wEjol.znu0xwxtnJ.TXYCbwGzu3IugzONz3XAf.E4JE3BjCu','Trần Đại Nghĩa','CUSTOMER', true,'2025-09-07 09:00:00'),
     ('hongtham@gmail.com','$2a$10$H72E4wEjol.znu0xwxtnJ.TXYCbwGzu3IugzONz3XAf.E4JE3BjCu','Hồng Thắm','CUSTOMER', true,'2025-09-08 09:00:00'),
     ('thanhtuyen@gmail.com','$2a$10$H72E4wEjol.znu0xwxtnJ.TXYCbwGzu3IugzONz3XAf.E4JE3BjCu','Thanh Tuyền','CUSTOMER', true,'2025-09-09 09:00:00');

INSERT INTO customers (user_id, phone, create_at) VALUES
      (2,'0923456789','2025-09-01 09:00:00'),
      (3,'0934567890','2025-09-02 09:00:00'),
      (4,'0945678901','2025-09-03 09:00:00'),
      (5,'0956789012','2025-09-04 09:00:00'),
      (6,'0967890123','2025-09-05 09:00:00'),
      (7,'0978901234','2025-09-06 09:00:00'),
      (8,'0989012345','2025-09-07 09:00:00'),
      (9,'0990123456','2025-09-08 09:00:00'),
      (10,'0901234567','2025-09-09 09:00:00');

INSERT INTO courts (court_name, court_type, status, hourly_rate, description, images, is_active, create_at, update_at) VALUES
    ('Sân cầu lông số 1', 'INDOOR', 'AVAILABLE', 100000.00, 'Sân cầu lông trong nhà', '["/court/img/a1edbc07-43d5-4adc-9928-c28a533e84db_ways-station-badminton-10-20241116112248-cyd6_.jpg"]', TRUE, '2025-08-20 10:00:00', '2025-08-20 10:00:00'),
    ('Sân cầu lông số 2', 'INDOOR', 'AVAILABLE', 100000.00, 'Sân cầu lông trong nhà', '["/court/img/61c43ecb-47d7-4874-8669-a7112cc6466f_ways-station-badminton-10-20241116112248-cyd6_.jpg"]', TRUE, '2025-08-20 10:00:00', '2025-08-20 10:00:00'),
    ('Sân cầu lông số 3', 'INDOOR', 'AVAILABLE', 100000.00, 'Sân cầu lông trong nhà', '["/court/img/b7fcceff-522b-4f23-9c71-08df9f69c086_ways-station-badminton-10-20241116112248-cyd6_.jpg"]', TRUE, '2025-08-20 10:00:00', '2025-08-20 10:00:00'),
    ('Sân cầu lông số 4', 'INDOOR', 'AVAILABLE', 100000.00, 'Sân cầu lông trong nhà', '["/court/img/56cb4475-ce2f-4dba-b233-f4275134f4d8_ways-station-badminton-10-20241116112248-cyd6_.jpg"]', TRUE, '2025-08-20 10:00:00', '2025-08-20 10:00:00'),
    ('Sân cầu lông số 5', 'INDOOR', 'AVAILABLE', 100000.00, 'Sân cầu lông trong nhà', '["/court/img/2f05a11d-1fa1-4472-921b-407977350602_ways-station-badminton-10-20241116112248-cyd6_.jpg"]', TRUE, '2025-08-20 10:00:00', '2025-08-20 10:00:00'),
    ('Sân cầu lông số 6', 'OUTDOOR', 'AVAILABLE', 150000.00, 'Sân cầu lông trong nhà', '["/court/img/d043db7a-564f-4901-acbc-d42d85fb235e_ways-station-badminton-10-20241116112248-cyd6_.jpg"]', TRUE, '2025-08-20 10:00:00', '2025-08-20 10:00:00'),
    ('Sân cầu lông số 7', 'OUTDOOR', 'AVAILABLE', 150000.00, 'Sân cầu lông trong nhà', '["/court/img/7bb801ef-9709-4024-9d5f-57405ed663f0_ways-station-badminton-10-20241116112248-cyd6_.jpg"]', TRUE, '2025-08-20 10:00:00', '2025-08-20 10:00:00'),
    ('Sân cầu lông số 8', 'OUTDOOR', 'AVAILABLE', 150000.00, 'Sân cầu lông trong nhà', '["/court/img/9b0ff7d9-74b6-4a21-9a7f-e8a680460996_ways-station-badminton-10-20241116112248-cyd6_.jpg"]', TRUE, '2025-08-20 10:00:00', '2025-08-20 10:00:00'),
    ('Sân cầu lông số 9', 'OUTDOOR', 'AVAILABLE', 150000.00, 'Sân cầu lông trong nhà', '["/court/img/d20843b3-b8a9-4535-8735-30b483f6c7a3_ways-station-badminton-10-20241116112248-cyd6_.jpg"]', TRUE, '2025-08-20 10:00:00', '2025-08-20 10:00:00'),
    ('Sân cầu lông số 10', 'OUTDOOR', 'AVAILABLE', 150000.00, 'Sân cầu lông trong nhà', '["/court/img/8f98cd4a-f5d3-4fe6-9f9a-a875f4719783_ways-station-badminton-10-20241116112248-cyd6_.jpg"]', TRUE, '2025-08-20 10:00:00', '2025-08-20 10:00:00');

INSERT INTO services (service_name, service_type, description, unit_price) VALUES
    ('Thuê sân cầu lông 1 giờ', 'Thiết bị', 'Dịch vụ cho thuê sân theo giờ chuẩn thi đấu', 100000),
    ('Thuê vợt cầu lông', 'Thiết bị', 'Cho thuê vợt cầu lông chất lượng cao', 40000),
    ('Cầu lông', 'Thiết bị', 'Cung cấp cầu lông dùng trong tập luyện và thi đấu', 50000),
    ('Băng keo thể thao', 'Thiết bị', 'Băng dán hỗ trợ khớp khi vận động', 15000),
    ('Nước uống (chai 500ml)', 'sản phẩm', 'Nước suối đóng chai 500ml', 10000),
    ('Nước uống thể thao', 'sản phẩm', 'Nước bù khoáng, ion cho vận động viên', 15000),
    ('Khăn tắm', 'Sản phẩm', 'Bán khăn tắm thể thao', 100000),
    ('Huấn luyện cơ bản', 'Đào tạo', 'Khóa huấn luyện cầu lông cơ bản theo giờ', 200000),
    ('Huấn luyện nâng cao', 'Đào tạo', 'Khóa huấn luyện cầu lông nâng cao theo giờ', 300000);



INSERT INTO bookings (booking_code, customer_id, court_id, booking_date, start_time, end_time, booking_status, total_amount, create_at, update_at, payment_status) VALUES
    ('BK-20250909-1110', 2, 1, '2025-09-10', '08:00:00', '10:00:00', 'CONFIRMED', 200000.00, '2025-09-09 10:00:00', '2025-09-10 07:00:00', 'PAID'),
    ('BK-20250909-1111', 3, 1, '2025-09-10', '10:00:00', '12:00:00', 'CONFIRMED', 200000.00, '2025-09-09 10:00:00', '2025-09-10 09:00:00', 'PAID'),
    ('BK-20250909-1112', 4, 1, '2025-09-10', '13:00:00', '14:00:00', 'CONFIRMED', 100000.00, '2025-09-09 10:00:00', '2025-09-10 12:00:00', 'PAID'),
    ('BK-20250909-1113', 5, 1, '2025-09-10', '14:00:00', '16:00:00', 'CONFIRMED', 200000.00, '2025-09-09 10:00:00', '2025-09-10 13:00:00', 'PAID'),
    ('BK-20250909-1114', 6, 1, '2025-09-10', '16:00:00', '18:00:00', 'CONFIRMED', 200000.00, '2025-09-09 10:00:00', '2025-09-10 15:00:00', 'PAID'),
    ('BK-20250909-1115', 7, 1, '2025-09-10', '18:00:00', '19:00:00', 'CONFIRMED', 100000.00, '2025-09-09 10:00:00', '2025-09-10 17:00:00', 'PAID'),
    ('BK-20250909-1116', 8, 1, '2025-09-10', '19:00:00', '20:00:00', 'CONFIRMED', 100000.00, '2025-09-09 10:00:00', '2025-09-10 18:00:00', 'PAID'),
    ('BK-20250909-1117', 9, 1, '2025-09-10', '20:00:00', '22:00:00', 'CONFIRMED', 200000.00, '2025-09-09 10:00:00', '2025-09-10 19:00:00', 'PAID'),
    ('BK-20250910-1118', 2, 2, '2025-09-11', '08:00:00', '10:00:00', 'CONFIRMED', 200000.00, '2025-09-10 10:00:00', '2025-09-11 07:00:00', 'PAID'),
    ('BK-20250910-1119', 3, 2, '2025-09-11', '10:00:00', '12:00:00', 'CONFIRMED', 200000.00, '2025-09-10 10:00:00', '2025-09-11 09:00:00', 'PAID'),
    ('BK-20250910-1120', 4, 2, '2025-09-11', '13:00:00', '14:00:00', 'CONFIRMED', 100000.00, '2025-09-10 10:00:00', '2025-09-11 12:00:00', 'PAID'),
    ('BK-20250910-1121', 5, 2, '2025-09-11', '14:00:00', '16:00:00', 'CONFIRMED', 200000.00, '2025-09-10 10:00:00', '2025-09-11 13:00:00', 'PAID'),
    ('BK-20250910-1122', 6, 2, '2025-09-11', '16:00:00', '18:00:00', 'CONFIRMED', 200000.00, '2025-09-10 10:00:00', '2025-09-11 15:00:00', 'PAID'),
    ('BK-20250910-1123', 7, 2, '2025-09-11', '18:00:00', '19:00:00', 'CONFIRMED', 100000.00, '2025-09-10 10:00:00', '2025-09-11 17:00:00', 'PAID'),
    ('BK-20250910-1124', 8, 2, '2025-09-11', '19:00:00', '20:00:00', 'CONFIRMED', 100000.00, '2025-09-10 10:00:00', '2025-09-11 18:00:00', 'PAID'),
    ('BK-20250910-1125', 9, 2, '2025-09-11', '20:00:00', '22:00:00', 'CONFIRMED', 200000.00, '2025-09-10 10:00:00', '2025-09-11 19:00:00', 'PAID'),

    ('BK-20250918-1126', 2, 5, '2025-09-19', '08:00:00', '10:00:00', 'CONFIRMED', 300000.00, '2025-09-18 10:00:00', '2025-09-19 07:00:00', 'PAID'),
    ('BK-20250918-1127', 3, 5, '2025-09-19', '10:00:00', '12:00:00', 'CONFIRMED', 300000.00, '2025-09-18 10:00:00', '2025-09-19 09:00:00', 'PAID'),
    ('BK-20250918-1128', 4, 5, '2025-09-19', '13:00:00', '14:00:00', 'CONFIRMED', 150000.00, '2025-09-18 10:00:00', '2025-09-19 12:00:00', 'PAID'),
    ('BK-20250918-1129', 5, 5, '2025-09-19', '14:00:00', '16:00:00', 'CONFIRMED', 300000.00, '2025-09-18 10:00:00', '2025-09-19 13:00:00', 'PAID'),
    ('BK-20250918-1130', 6, 5, '2025-09-19', '16:00:00', '18:00:00', 'CONFIRMED', 300000.00, '2025-09-18 10:00:00', '2025-09-19 15:00:00', 'PAID'),
    ('BK-20250918-1131', 7, 5, '2025-09-19', '18:00:00', '19:00:00', 'CONFIRMED', 150000.00, '2025-09-18 10:00:00', '2025-09-19 17:00:00', 'PAID'),
    ('BK-20250918-1132', 8, 5, '2025-09-19', '19:00:00', '20:00:00', 'CONFIRMED', 150000.00, '2025-09-18 10:00:00', '2025-09-19 18:00:00', 'PAID'),
    ('BK-20250918-1133', 9, 5, '2025-09-19', '20:00:00', '22:00:00', 'CONFIRMED', 300000.00, '2025-09-18 10:00:00', '2025-09-19 19:00:00', 'PAID'),

    ('BK-20250919-1134', 2, 6, '2025-09-20', '08:00:00', '10:00:00', 'CONFIRMED', 300000.00, '2025-09-19 10:00:00', '2025-09-20 07:00:00', 'PAID'),
    ('BK-20250919-1135', 3, 6, '2025-09-20', '10:00:00', '12:00:00', 'CONFIRMED', 300000.00, '2025-09-19 10:00:00', '2025-09-20 09:00:00', 'PAID'),
    ('BK-20250919-1136', 4, 6, '2025-09-20', '13:00:00', '14:00:00', 'CANCELLED', 150000.00, '2025-09-19 10:00:00', '2025-09-20 12:00:00', 'UNPAID'),
    ('BK-20250919-1137', 5, 6, '2025-09-20', '14:00:00', '16:00:00', 'CANCELLED', 300000.00, '2025-09-19 10:00:00', '2025-09-20 13:00:00', 'UNPAID'),
    ('BK-20250919-1138', 6, 6, '2025-09-20', '16:00:00', '18:00:00', 'CANCELLED', 300000.00, '2025-09-19 10:00:00', '2025-09-20 15:00:00', 'UNPAID'),
    ('BK-20250919-1139', 7, 6, '2025-09-20', '18:00:00', '19:00:00', 'PENDING', 150000.00, '2025-09-19 10:00:00', '2025-09-20 17:00:00', 'UNPAID'),
    ('BK-20250919-1140', 8, 6, '2025-09-20', '19:00:00', '20:00:00', 'PENDING', 150000.00, '2025-09-19 10:00:00', '2025-09-20 18:00:00', 'UNPAID'),
    ('BK-20250919-1141', 9, 6, '2025-09-20', '20:00:00', '22:00:00', 'PENDING', 300000.00, '2025-09-19 10:00:00', '2025-09-20 19:00:00', 'UNPAID');

INSERT INTO transactions (amount, transaction_date, payment_method, booking_id, created_by, create_at, update_at)
VALUES
    (200000.00, '2025-09-10', 'COD', 1, 1, '2025-09-10 07:00:00', '2025-09-10 07:00:00'),
    (200000.00, '2025-09-10', 'TRANSFER', 2, 1, '2025-09-10 09:00:00', '2025-09-10 09:00:00'),
    (100000.00, '2025-09-10', 'COD', 3, 1, '2025-09-10 12:00:00', '2025-09-10 12:00:00'),
    (200000.00, '2025-09-10', 'TRANSFER', 4, 1, '2025-09-10 13:00:00', '2025-09-10 13:00:00'),
    (200000.00, '2025-09-10', 'COD', 5, 1, '2025-09-10 15:00:00', '2025-09-10 15:00:00'),
    (100000.00, '2025-09-10', 'TRANSFER', 6, 1, '2025-09-10 17:00:00', '2025-09-10 17:00:00'),
    (100000.00, '2025-09-10', 'COD', 7, 1, '2025-09-10 18:00:00', '2025-09-10 18:00:00'),
    (200000.00, '2025-09-10', 'TRANSFER', 8, 1, '2025-09-10  19:00:00', '2025-09-10  19:00:00'),
    (200000.00, '2025-09-11', 'COD', 9, 1, '2025-09-11 07:00:00', '2025-09-11 07:00:00'),
    (200000.00, '2025-09-11', 'TRANSFER', 10, 1, '2025-09-11 09:00:00', '2025-09-11 09:00:00'),
    (100000.00, '2025-09-11', 'COD', 11, 1, '2025-09-11 12:00:00', '2025-09-11 12:00:00'),
    (200000.00, '2025-09-11', 'TRANSFER', 12, 1, '2025-09-11 13:00:00', '2025-09-11 13:00:00'),
    (200000.00, '2025-09-11', 'COD', 13, 1, '2025-09-11 15:00:00', '2025-09-11 15:00:00'),
    (100000.00, '2025-09-11', 'TRANSFER', 14, 1, '2025-09-11 17:00:00', '2025-09-11 17:00:00'),
    (100000.00, '2025-09-11', 'COD', 15, 1, '2025-09-11 18:00:00', '2025-09-11 18:00:00'),
    (200000.00, '2025-09-11', 'TRANSFER', 16, 1, '2025-09-11  19:00:00', '2025-09-11  19:00:00'),
    (300000.00, '2025-09-19', 'COD', 17, 1, '2025-09-19 07:00:00', '2025-09-19 07:00:00'),
    (300000.00, '2025-09-19', 'TRANSFER', 18, 1, '2025-09-19 09:00:00', '2025-09-19 09:00:00'),
    (150000.00, '2025-09-19', 'COD', 19, 1, '2025-09-19 12:00:00', '2025-09-19 12:00:00'),
    (300000.00, '2025-09-19', 'TRANSFER', 20, 1, '2025-09-19 13:00:00', '2025-09-19 13:00:00'),
    (300000.00, '2025-09-19', 'COD', 21, 1, '2025-09-19 15:00:00', '2025-09-19 15:00:00'),
    (150000.00, '2025-09-19', 'TRANSFER', 22, 1, '2025-09-19 17:00:00', '2025-09-19 17:00:00'),
    (150000.00, '2025-09-19', 'COD', 23, 1, '2025-09-19 18:00:00', '2025-09-19 18:00:00'),
    (300000.00, '2025-09-19', 'TRANSFER', 24, 1, '2025-09-19  19:00:00', '2025-09-19  19:00:00'),
    (300000.00, '2025-09-20', 'COD', 25, 1, '2025-09-20 18:00:00', '2025-09-20 18:00:00'),
    (300000.00, '2025-09-20', 'TRANSFER', 26, 1, '2025-09-20  19:00:00', '2025-09-20  19:00:00');
