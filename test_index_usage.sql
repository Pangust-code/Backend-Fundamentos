-- Consulta optimizada que SÍ usa índice
EXPLAIN ANALYZE 
SELECT * FROM products 
WHERE name LIKE 'Laptop%'
ORDER BY price DESC 
LIMIT 5;
