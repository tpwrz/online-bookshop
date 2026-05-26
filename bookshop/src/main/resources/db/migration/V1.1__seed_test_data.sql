-- GENRES
INSERT INTO genre (name)
SELECT name
FROM (VALUES ('Science Fiction'),
             ('Fantasy'),
             ('Romance'),
             ('Mystery'),
             ('History')) AS t(name)
WHERE NOT EXISTS (SELECT 1 FROM genre);

-- PERSONS
INSERT INTO persons (first_name, middle_name, last_name, address, telephone_number, birth_date)
SELECT *
FROM (VALUES ('John', 'A', 'Doe', '123 Main St', '111-222-333', DATE '1980-01-01'),
             ('Jane', NULL, 'Smith', '456 Oak Ave', '444-555-666', DATE '1990-02-02'),
             ('Robert', 'B', 'Brown', '789 Pine Rd', '777-888-999', DATE '1975-03-03')) AS t(first_name, middle_name,
                                                                                             last_name, address,
                                                                                             telephone_number,
                                                                                             birth_date)
WHERE NOT EXISTS (SELECT 1 FROM persons);

-- USERS
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';
-- Insert admin person
INSERT INTO persons (first_name, middle_name, last_name, address, telephone_number, birth_date)
SELECT 'Admin', NULL, 'Bookshop', 'Admin St 1', '000-000-000', DATE '1990-01-01'
WHERE NOT EXISTS (SELECT 1 FROM persons WHERE last_name = 'Bookshop');
-- Insert admin user (password: Admin1234! BCrypt cost 12)
INSERT INTO users (status, email, registration_date, person_id, username, password, role)
SELECT 'ACTIVE',
       'admin@bookshop.com',
       CURRENT_DATE,
       p.id,
       'admin',
       '$2a$12$JlAjNdwgzqXEq/7MGhvft.bJGevcWJrn1ZEqzdTN6sHjeYu5ugj.6',
       'ADMIN'
FROM persons p
WHERE p.last_name = 'Bookshop'
  AND NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');
INSERT INTO users (status, email, registration_date, person_id, username, password, role)
SELECT 'ACTIVE', 'john@example.com', CURRENT_DATE, p.id, 'john_doe', 'Password1!', 'USER'
FROM persons p
WHERE p.first_name = 'John'
  AND NOT EXISTS (SELECT 1 FROM users);

INSERT INTO users (status, email, registration_date, person_id, username, password, role)
SELECT 'ACTIVE', 'jane@example.com', CURRENT_DATE, p.id, 'jane_smith', 'Password1!', 'USER'
FROM persons p
WHERE p.first_name = 'Jane'
  AND NOT EXISTS (SELECT 1 FROM users WHERE username = 'jane_smith');

-- BOOKS
INSERT INTO book (title, genre_id, price, currency, language, page_number, availability, average_rating, author_id,
                  release_date)
SELECT 'The Future of Us',
       g.id,
       19.99,
       'USD',
       'ENGLISH',
       320,
       'AVAILABLE',
       4.5,
       p.id,
       '2020-05-01'
FROM genre g,
     persons p
WHERE g.name = 'Science Fiction'
  AND p.first_name = 'John'
  AND NOT EXISTS (SELECT 1 FROM book WHERE title = 'The Future of Us');

INSERT INTO book (title, genre_id, price, currency, language, page_number, availability, average_rating, author_id,
                  release_date)
SELECT 'Mystic Tales',
       g.id,
       14.99,
       'EUR',
       'ROMANIAN',
       250,
       'PREORDER',
       4.0,
       p.id,
       '2022-08-15'
FROM genre g,
     persons p
WHERE g.name = 'Fantasy'
  AND p.first_name = 'Jane'
  AND NOT EXISTS (SELECT 1 FROM book WHERE title = 'Mystic Tales');

-- ORDERS
INSERT INTO orders (ship_address, order_date, order_status, user_id)
SELECT '123 Main St', CURRENT_DATE, 'NEW', u.id
FROM users u
WHERE u.username = 'john_doe'
  AND NOT EXISTS (SELECT 1 FROM orders WHERE user_id = u.id);

INSERT INTO orders (ship_address, order_date, order_status, user_id)
SELECT '456 Oak Ave', CURRENT_DATE, 'DELIVERED', u.id
FROM users u
WHERE u.username = 'jane_smith'
  AND NOT EXISTS (SELECT 1 FROM orders WHERE user_id = u.id);

-- ORDER ITEMS
INSERT INTO order_items (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, 2, b.price
FROM orders o,
     book b
WHERE o.user_id = (SELECT id FROM users WHERE username = 'john_doe')
  AND b.title = 'The Future of Us'
  AND NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = o.id AND book_id = b.id);

INSERT INTO order_items (order_id, book_id, quantity, unit_price)
SELECT o.id, b.id, 1, b.price
FROM orders o,
     book b
WHERE o.user_id = (SELECT id FROM users WHERE username = 'jane_smith')
  AND b.title = 'Mystic Tales'
  AND NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = o.id AND book_id = b.id);

-- REVIEWS
INSERT INTO reviews (user_id, book_id, review_message, review_rating)
SELECT u.id, b.id, 'Great read!', 5
FROM users u,
     book b
WHERE u.username = 'john_doe'
  AND b.title = 'The Future of Us'
  AND NOT EXISTS (SELECT 1 FROM reviews WHERE user_id = u.id AND book_id = b.id);

INSERT INTO reviews (user_id, book_id, review_message, review_rating)
SELECT u.id, b.id, 'Enjoyed it!', 4
FROM users u,
     book b
WHERE u.username = 'jane_smith'
  AND b.title = 'Mystic Tales'
  AND NOT EXISTS (SELECT 1 FROM reviews WHERE user_id = u.id AND book_id = b.id);
