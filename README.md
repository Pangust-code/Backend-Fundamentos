# 9. Resultados y Evidencias Requeridas

## 9.1. Evidencias de implementación
- [ ] **Captura de `ProductController.java`**: Debe mostrar los endpoints paginados implementados.
- [ ] **Captura de `ProductService.java`**: Debe mostrar los métodos de lógica de paginación.
- [ ] **Captura de `ProductRepository.java`**: Debe evidenciar las consultas `@Query` integradas con `Pageable`.
- [ ] **Captura de SQL generado**: Logs de la consola mostrando las cláusulas `LIMIT` y `OFFSET`.

## 9.2. Evidencias de funcionamiento
- [ ] **Page response**:
  - Endpoint: `GET /api/products?page=0&size=5`
  - *Requisito:* Mostrar metadatos completos (`totalPages`, `totalElements`, etc.).
- [ ] **Slice response**:
  - Endpoint: `GET /api/products/slice?page=0&size=5`
  - *Requisito:* Respuesta sin el cálculo de `totalElements`.
- [ ] **Filtros + paginación**:
  - Endpoint: `GET /api/products/search?name=laptop&page=0&size=3`
- [ ] **Ordenamiento**:
  - Endpoint: `GET /api/products?sort=price,desc&page=1&size=5`

## 9.3. Evidencias de performance
- [ ] **Índices**: Captura del comando `EXPLAIN` (base de datos) mostrando el uso efectivo de índices.
- [ ] **Comparación**: Tabla o captura comparando tiempos de respuesta entre *Page* vs *Slice*.
- [ ] **Consultas SQL**: Logs que muestren las consultas `COUNT` automáticas generadas por el framework.

## 9.4. Datos para revisión

### Dataset Base
Se requiere un dataset de al menos **100 productos** con las siguientes características:
- [ ] 20+ productos por usuario.
- [ ] 10+ productos por categoría.
- [ ] Precios variados (Rango: $10 - $5000).
- [ ] Nombres con texto buscable (para pruebas de filtros).

### Escenarios de prueba (Volumen)
Ejecutar y documentar las siguientes consultas:
- [ ] Primera página de productos (`page=0, size=10`).
- [ ] Página intermedia (`page=5, size=10`).
- [ ] Últimas páginas (para verificar performance en offsets altos).
- [ ] Búsquedas que arrojen pocos resultados vs. muchos resultados.
- [ ] Orden