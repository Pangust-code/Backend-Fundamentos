-- =====================================================
-- Script: Comparación de Performance CON y SIN índices
-- =====================================================

-- PARTE 1: Consulta ANTES de crear índices
-- Ejecutar esto primero con run_sql.py

EXPLAIN ANALYZE 
SELECT * FROM products 
WHERE name LIKE '%Laptop%' 
ORDER BY price DESC 
LIMIT 5 OFFSET 10;

-- RESULTADO ESPERADO SIN ÍNDICE:
-- -> Seq Scan (escaneo completo de la tabla)
-- -> Sort en memoria
-- -> Tiempo: ~0.2-0.5ms (depende del tamaño)

-- =====================================================
-- AHORA EJECUTA: python run_sql.py create_indexes.sql
-- =====================================================

-- PARTE 2: Consulta DESPUÉS de crear índices
-- Ejecutar esto después de crear los índices

EXPLAIN ANALYZE 
SELECT * FROM products 
WHERE name LIKE '%Laptop%' 
ORDER BY price DESC 
LIMIT 5 OFFSET 10;

-- RESULTADO ESPERADO CON ÍNDICE:
-- -> Puede seguir siendo Seq Scan porque LIKE '%text%' no usa índice
-- -> Pero el Sort será más rápido si hay índice en 'price'

-- =====================================================
-- CONSULTA OPTIMIZADA: Búsqueda que SÍ usa índices
-- =====================================================

EXPLAIN ANALYZE 
SELECT * FROM products 
WHERE name LIKE 'Laptop%'  -- SIN % al inicio
ORDER BY price DESC 
LIMIT 5 OFFSET 10;

-- ESTA consulta SÍ debería usar el índice idx_products_name

-- =====================================================
-- Consulta por rango de precios (usa índice)
-- =====================================================

EXPLAIN ANALYZE 
SELECT * FROM products 
WHERE price BETWEEN 100 AND 1000 
ORDER BY price DESC 
LIMIT 10;

-- ESTA consulta debería usar idx_products_price

-- =====================================================
-- Consulta por usuario (usa índice de FK)
-- =====================================================

EXPLAIN ANALYZE 
SELECT * FROM products 
WHERE user_id = 1 
ORDER BY created_at DESC 
LIMIT 10;

-- ESTA consulta debería usar idx_products_user_id
