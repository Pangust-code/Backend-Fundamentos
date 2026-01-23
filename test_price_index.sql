-- Consulta por rango de precios (usa índice idx_products_price)
EXPLAIN ANALYZE 
SELECT id, name, price FROM products 
WHERE price BETWEEN 1000 AND 2000 
ORDER BY price DESC 
LIMIT 10;
