# 9. Resultados y Evidencias Requeridas

## 9.1. Evidencias de implementación
- [ ] **Captura de `ProductController.java`**: Debe mostrar los endpoints paginados implementados.
![alt text](assets/{7E65FDAC-2358-4555-877A-3B2BFFA33484}.png)
![alt text](assets/{35C0342B-8F3F-4875-8C0E-B8EC8A5DBF4C}.png)
- [ ] **Captura de `ProductService.java`**: Debe mostrar los métodos de lógica de paginación.
![alt text](assets/{DD543151-0272-4F76-B29D-1F04FD076761}.png)
![alt text](assets/{11D177B5-775C-485B-ACE6-DBC6EB720A35}.png)
- [ ] **Captura de `ProductRepository.java`**: Debe evidenciar las consultas `@Query` integradas con `Pageable`.
![alt text](assets/{4129EB3D-35F4-408B-A369-8220EE144EAF}.png)
![alt text](assets/{50C411D7-1BAB-49DC-8641-EFC6C6CCA2C1}.png)
![alt text](assets/{6A2A6D76-62DD-4AF7-96BA-08C85BAFD165}.png)
![alt text]assets/({936CC397-8D99-46BA-B8CF-A200D5D55669}.png)
![alt text](assets/{A999E3B5-08D8-423A-9732-F305B209D8C1}.png)
![alt text](assets/{2485A8FA-6456-4099-B67F-1CC593AF9ED8}.png)
![alt text](assets/{8CA58EE1-BA96-496D-AF08-60E542C2D1B4}.png)
- [ ] **Captura de SQL generado**: Logs de la consola mostrando las cláusulas `LIMIT` y `OFFSET`.

![alt text](assets/{8307FF86-AB7C-466A-9B8C-9C26A82A82FC}.png)
![alt text](assets/{55AF2B69-A22C-4A44-9078-6A7F57155C81}.png)
![alt text](assets/{C2C80E1D-A4EB-4218-86F9-126C2697E570}.png)
![alt text](assets/{26338465-6084-42F7-BAD5-ECF891594C89}.png)

## 9.2. Evidencias de funcionamiento
- [ ] **Page response**:
  - Endpoint: `GET /api/products?page=0&size=5`
![alt text](assets/{7E477D48-8675-46D4-8246-CDE3EA3BFE7C}.png)
- [ ] **Slice response**:
  - Endpoint: `GET /api/products/slice?page=0&size=5`
![alt text](assets/{C2FC5BC2-D75A-4DE5-92FE-4BB3FF929EAC}.png)
- [ ] **Filtros + paginación**:
  - Endpoint: `GET /api/products/search?name=laptop&page=0&size=3`
![alt text](assets/{A00A0B0B-1C84-4E9F-B0E8-8DA36A6B5C3B}.png)
- [ ] **Ordenamiento**:
  - Endpoint: `GET /api/products?sort=price,desc&page=1&size=5`
![alt text](assets/{250DDEE3-13CE-43B1-AE72-F6BD0A432430}.png)

## 9.3. Evidencias de performance
- [ ] **Índices**: Captura del comando `EXPLAIN` (base de datos) mostrando el uso efectivo de índices.
![alt text](assets/{1D32E2EE-C58A-4689-85DF-4ECCC07D3AFD}.png)
- [ ] **Comparación**: Tabla o captura comparando tiempos de respuesta entre *Page* vs *Slice*.
- *Page*
![alt text](assets/{706399FC-28A5-449A-8080-2113444F78F3}.png)
- *Slice*
![alt text](assets/{DC7F229F-A713-412D-8EDC-23A0F7534861}.png)

- [ ] **Consultas SQL**: Logs que muestren las consultas `COUNT` automáticas generadas por el framework.

![alt text](assets/{40BF8D9E-AF7B-4AD0-94DC-E468D7509CDC}.png)

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
- *page*
![alt text](assets/{B75C31F2-233A-43E2-B3AD-791D3AE5099B}.png)
![alt text](assets/{DFE61475-A04C-4AEF-BABC-DA84C93774A1}.png)
-*slice*
![alt text](assets/{3F42E3B5-C112-4E3D-A8D4-20B7DB7BF5E1}.png)
![alt text](assets/{0EFFE920-FE8C-4D9C-8283-81B626626EE1}.png)
- [ ] Últimas páginas (para verificar performance en offsets altos).
-*page*
![alt text](assets/{6148D581-FBA7-4222-AE70-0A47795F4AC5}.png)
-*slice*
![alt text](assets/{278F108B-CCBE-43BE-96D2-48192DB05052}.png)
- [ ] Búsquedas que arrojen pocos resultados vs. muchos resultados.
- [ ] Orden
![alt text](assets/{4442389B-F0C3-4993-BFEF-332B4A103C9C}.png)

# 10. Subir comiit de us poreycto con captura en readme del consumo de 

- *login*
![alt text](assets/{0101D069-848B-4BE7-A756-0FE41B9EAEFD}.png)
- *register*
![alt text](assets/{4563A927-A8EE-489E-8A7C-CEF319164631}.png)
- *api/users SIN TOKEN*
![alt text](assets/{683D800E-4D49-48C7-BF08-B376EC819AC9}.png)
