-- =====================================================
-- Script: Creación de Índices para Performance
-- =====================================================

-- 1. Índice en columna 'name' (para búsquedas con LIKE y filtros)
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- 2. Índice en columna 'price' (para ordenamiento y filtros de rango)
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);

-- 3. Índice compuesto para búsquedas + ordenamiento
CREATE INDEX IF NOT EXISTS idx_products_name_price ON products(name, price);

-- 4. Índice en 'user_id' (para búsquedas por usuario)
CREATE INDEX IF NOT EXISTS idx_products_user_id ON products(user_id);

-- 5. Índice en 'created_at' (para ordenamiento temporal)
CREATE INDEX IF NOT EXISTS idx_products_created_at ON products(created_at);

-- Verificar índices creados
SELECT 
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'products'
ORDER BY indexname;
