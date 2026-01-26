package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.reporitory.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;

import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.mappers.ProductMapper;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;

import ec.edu.ups.icc.fundamentos01.products.models.Product;
import ec.edu.ups.icc.fundamentos01.products.models.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.repository.ProductRepository;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;
import ec.edu.ups.icc.fundamentos01.users.models.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repository.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;

    private final UserRepository userRepo;

    private final CategoryRepository categoryRepo;

    public ProductServiceImpl(ProductRepository productRepo,
            UserRepository userRepo,
            CategoryRepository categoryRepository) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepository;
        this.userRepo = userRepo;
    }

        // ============== MÉTODOS DE CONSULTA ==============


    // ============== MÉTODOS DE MODIFICACIÓN CON VALIDACIÓN DE OWNERSHIP ==============

    /**
     * Actualizar producto con validación de ownership
     * 
     * Validación:
     * - Si eres ADMIN o MODERATOR → Puedes actualizar cualquier producto
     * - Si eres USER → Solo puedes actualizar TUS productos
     * 
     * @param id ID del producto a actualizar
     * @param dto Datos para actualizar
     * @param currentUser Usuario autenticado (del JWT)
     * @throws AccessDeniedException si no eres dueño ni tienes rol privilegiado
     */
    @Transactional
    public ProductResponseDto update(Long id, UpdateProductDto dto, UserDetailsImpl currentUser) {


        // 1. BUSCAR PRODUCTO EXISTENTE

        ProductEntity product = findProductOrThrow(id);
        // 2. VALIDACIÓN DE OWNERSHIP (pasando el usuario)
        validateOwnership(product, currentUser);

        // Si pasa la validación, actualizar
        // 3. VALIDAR Y OBTENER CATEGORÍAS

        Set<CategoryEntity> categories = validateAndGetCategories(dto.categoryIds);

        // 4. ACTUALIZAR USANDO DOMINIO

        Product productDomain = Product.fromEntity(product);
        productDomain.update(dto);

        // 5. CONVERTIR A ENTIDAD MANTENIENDO OWNER ORIGINAL

        ProductEntity updated = productDomain.toEntity(product.getOwner(), categories);
        updated.setId(id); // Asegurar que mantiene el ID
        
        // 6. PERSISTIR Y RESPONDER
        ProductEntity saved = productRepo.save(updated);

        return ProductMapper.toDto(saved);
    }

    /**
     * Eliminar producto con validación de ownership
     * 
     * Validación:
     * - Si eres ADMIN o MODERATOR → Puedes eliminar cualquier producto
     * - Si eres USER → Solo puedes eliminar TUS productos
     * 
     * @param id ID del producto a eliminar
     * @param currentUser Usuario autenticado (del JWT)
     * @throws AccessDeniedException si no eres dueño ni tienes rol privilegiado
     */
    @Transactional
    public void delete(Long id, UserDetailsImpl currentUser) {
        ProductEntity product = findProductOrThrow(id);
        
        // ← VALIDACIÓN DE OWNERSHIP (pasando el usuario)
        validateOwnership(product, currentUser);

        // Si pasa la validación, eliminar
        productRepo.delete(product);
    }

    // ============== MÉTODOS DE VALIDACIÓN Y UTILIDADES ==============

    private ProductEntity findProductOrThrow(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));
    }

    /**
     * Valida si el usuario puede modificar/eliminar el producto
     * 
     * Lógica:
     * 1. Si tiene ROLE_ADMIN → Puede modificar cualquier producto
     * 2. Si tiene ROLE_MODERATOR → Puede modificar cualquier producto
     * 3. Si es ROLE_USER → Solo puede modificar sus propios productos
     * 
     * @param product Producto a validar
     * @param currentUser Usuario autenticado (del JWT)
     * @throws AccessDeniedException si no tiene permisos
     */
    private void validateOwnership(ProductEntity product, UserDetailsImpl currentUser) {
        // ADMIN y MODERATOR pueden modificar cualquier producto
        if (hasAnyRole(currentUser, "ROLE_ADMIN", "ROLE_MODERATOR")) {
            return;  // ← Pasa la validación automáticamente
        }

        // USER solo puede modificar sus propios productos
        if (!product.getOwner().getId().equals(currentUser.getId())) {
            // ← Lanza excepción que será capturada por GlobalExceptionHandler
            throw new AccessDeniedException("No puedes modificar productos ajenos");
        }

        // Si llega aquí, es el dueño → Pasa la validación
    }

    /**
     * Verifica si el usuario tiene alguno de los roles especificados
     * 
     * @param user Usuario a verificar
     * @param roles Roles a buscar
     * @return true si tiene al menos uno de los roles
     */
    private boolean hasAnyRole(UserDetailsImpl user, String... roles) {
        for (String role : roles) {
            for (GrantedAuthority authority : user.getAuthorities()) {
                if (authority.getAuthority().equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

 

    /**
     * Crea Pageable con ordenamiento dinámico
     */
    private Pageable createPageable(int page, int size, String[] sort) {
        String sortField = sort[0];
        Sort.Direction sortDirection = sort.length > 1 && sort[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(sortDirection, sortField));
    }

    @Override
    public ProductResponseDto create(CreateProductDto dto) {

        // 1. VALIDAR EXISTENCIA DE RELACIONES
        UserEntity owner = userRepo.findById(dto.userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + dto.userId));

        // 2. VALIDAR Y OBTENER CATEGORÍAS
        Set<CategoryEntity> categories = validateAndGetCategories(dto.categoryIds);

        // Regla: nombre único
        if (productRepo.findByName(dto.name).isPresent()) {
            throw new IllegalStateException("El nombre del producto ya está registrado");
        }

        // 2. CREAR MODELO DE DOMINIO
        Product product = Product.fromDto(dto);

        // 3. CONVERTIR A ENTIDAD CON RELACIONES
        ProductEntity entity = product.toEntity(owner, categories);

        // 4. PERSISTIR
        ProductEntity saved = productRepo.save(entity);

        // 5. CONVERTIR A DTO DE RESPUESTA
        return toResponseDto(saved);
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return productRepo.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    // ============== MÉTODOS HELPER ==============

    @Override
    public ProductResponseDto findById(Long id) {
        return productRepo.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));
    }

    @Override
    public List<ProductResponseDto> findByUserId(Long userId) {

        // Validar que el usuario existe
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException("Usuario no encontrado con ID: " + userId);
        }

        return productRepo.findByOwnerId(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public List<ProductResponseDto> findByCategoryId(Long categoryId) {

        // Validar que la categoría existe
        if (!categoryRepo.existsById(categoryId)) {
            throw new NotFoundException("Categoría no encontrada con ID: " + categoryId);
        }

        return productRepo.findByCategoriesId(categoryId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public ProductResponseDto update(Long id, UpdateProductDto dto) {

        // 1. BUSCAR PRODUCTO EXISTENTE
        ProductEntity existing = productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));

        // 2. VALIDAR Y OBTENER CATEGORÍAS
        Set<CategoryEntity> categories = validateAndGetCategories(dto.categoryIds);

        // 3. ACTUALIZAR USANDO DOMINIO
        Product product = Product.fromEntity(existing);
        product.update(dto);

        // 4. CONVERTIR A ENTIDAD MANTENIENDO OWNER ORIGINAL
        ProductEntity updated = product.toEntity(existing.getOwner(), categories);
        updated.setId(id); // Asegurar que mantiene el ID

        // 5. PERSISTIR Y RESPONDER
        ProductEntity saved = productRepo.save(updated);
        return toResponseDto(saved);
    }

    @Override
    public void delete(Long id) {

        ProductEntity product = productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));

        // Eliminación física (también se puede implementar lógica)
        productRepo.delete(product);
    }

    private ProductResponseDto toResponseDto(ProductEntity entity) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.price = entity.getPrice();
        dto.description = entity.getDescription();

        ProductResponseDto.UserSummaryDto ownerDto = new ProductResponseDto.UserSummaryDto();
        ownerDto.id = entity.getOwner().getId();
        ownerDto.name = entity.getOwner().getName();
        ownerDto.email = entity.getOwner().getEmail();

        List<CategoryResponseDto> categoryDtos = new ArrayList<>();
        for (CategoryEntity categoryEntity : entity.getCategories()) {
            CategoryResponseDto categoryDto = new CategoryResponseDto();
            categoryDto.id = categoryEntity.getId();
            categoryDto.name = categoryEntity.getName();
            categoryDto.description = categoryEntity.getDescription();
            categoryDtos.add(categoryDto);
        }
        dto.user = ownerDto;
        dto.categories = categoryDtos;
        
        // Mapear campos de auditoría
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        
        return dto;

    }

    private Set<CategoryEntity> validateAndGetCategories(Set<Long> categoryIds) {
        Set<CategoryEntity> categories = new HashSet<>();

        for (Long categoryId : categoryIds) {
            CategoryEntity category = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + categoryId));
            categories.add(category);
        }

        return categories;
    }

    @Override
    public Page<ProductResponseDto> findAllPaginated(int page, int size, String[] sort) {
        Pageable pageable = createPageable(page, size, sort);
        Page<ProductEntity> productPage = productRepo.findAll(pageable);
        
        return productPage.map(this::toResponseDto);

    }

    
    // private Pageable createPageable(int page, int size, String[] sort) {
    //     // Validar parámetros
    //     if (page < 0) {
    //         throw new BadRequestException("La página debe ser mayor o igual a 0");
    //     }
    //     if (size < 1 || size > 100) {
    //         throw new BadRequestException("El tamaño debe estar entre 1 y 100");
    //     }
        
    //     // Crear Sort
    //     Sort sortObj = createSort(sort);
        
    //     return PageRequest.of(page, size, sortObj);
    // }

        // private Sort createSort(String[] sort) {
        // if (sort == null || sort.length == 0) {
        //     return Sort.by("id");
        // }

    //     List<Sort.Order> orders = new ArrayList<>();
    //     for (String sortParam : sort) {
    //         String[] parts = sortParam.split(",");
    //         String property = parts[0];
    //         String direction = parts.length > 1 ? parts[1] : "asc";
            
    //         // Validar propiedades permitidas para evitar inyección SQL
    //         if (!isValidSortProperty(property)) {
    //             throw new BadRequestException("Propiedad de ordenamiento no válida: " + property);
    //         }
            
    //         Sort.Order order = "desc".equalsIgnoreCase(direction) 
    //             ? Sort.Order.desc(property)
    //             : Sort.Order.asc(property);
            
    //         orders.add(order);
    //     }
        
    //     return Sort.by(orders);
    // }

    // private boolean isValidSortProperty(String property) {
    //     // Lista blanca de propiedades permitidas para ordenamiento
    //     Set<String> allowedProperties = Set.of(
    //         "id", "name", "price", "createdAt", "updatedAt",
    //         "owner.name", "owner.email", "category.name"
    //     );
    //     return allowedProperties.contains(property);
    // }

    private void validateFilterParameters(Double minPrice, Double maxPrice) {
        if (minPrice != null && minPrice < 0) {
            throw new BadRequestException("El precio mínimo no puede ser negativo");
        }
        
        if (maxPrice != null && maxPrice < 0) {
            throw new BadRequestException("El precio máximo no puede ser negativo");
        }
        
        if (minPrice != null && maxPrice != null && maxPrice < minPrice) {
            throw new BadRequestException("El precio máximo debe ser mayor o igual al precio mínimo");
        }
    }

    @Override
    public Page<ProductResponseDto> findWithFilters(String name, Double minPrice, Double maxPrice, Long categoryId,
            int page, int size, String[] sort) {
        
        // Validaciones de filtros (del tema 09)
        validateFilterParameters(minPrice, maxPrice);
        
        // Crear Pageable
        Pageable pageable = createPageable(page, size, sort);
        
        // Consulta con filtros y paginación
        Page<ProductEntity> productPage = productRepo.findWithFilters(
            name, minPrice, maxPrice, categoryId, pageable);
        
        return productPage.map(this::toResponseDto);
    }

    @Override
    public Page<ProductResponseDto> findByUserIdWithFilters(Long userId, String name, Double minPrice, Double maxPrice,
            Long categoryId, int page, int size, String[] sort) {
        
        // 1. Validar que el usuario existe
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException("Usuario no encontrado con ID: " + userId);
        }
        
        // 2. Validar filtros
        validateFilterParameters(minPrice, maxPrice);
        
        // 3. Crear Pageable
        Pageable pageable = createPageable(page, size, sort);
        
        // 4. Consulta con filtros y paginación
        Page<ProductEntity> productPage = productRepo.findByUserIdWithFilters(
            userId, name, minPrice, maxPrice, categoryId, pageable);
        
        return productPage.map(this::toResponseDto);
    }

    @Override
    public Slice<ProductResponseDto> findAllSlice(int page, int size, String[] sort) {
        Pageable pageable = createPageable(page, size, sort);
        Slice<ProductEntity> productSlice = productRepo.findAll(pageable);
        
        return productSlice.map(this::toResponseDto);
    }

}