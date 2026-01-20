package ec.edu.ups.icc.fundamentos01.products.dtos;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.validation.constraints.*;

public class PageableDto {

    @Min(value = 0, message = "La página debe ser mayor o igual a 0")
    private int page = 0;

    @Min(value = 1, message = "El tamaño debe ser mayor a 0")
    @Max(value = 100, message = "El tamaño no puede ser mayor a 100")
    private int size = 10;

    private String[] sort = {"id"};

    // Constructores
    public PageableDto() {
    }

    public PageableDto(int page, int size, String[] sort) {
        this.page = page;
        this.size = size;
        this.sort = sort;
    }

    // Getters y setters
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String[] getSort() {
        return sort;
    }

    public void setSort(String[] sort) {
        this.sort = sort;
    }

    // ============== MÉTODO HELPER ==============

    /**
     * Convierte a PageRequest de Spring Data JPA
     */
    public Pageable toPageable() {
        return PageRequest.of(page, size, createSort());
    }

    private Sort createSort() {
        if (sort == null || sort.length == 0) {
            return Sort.by("id");
        }

        Sort.Order[] orders = new Sort.Order[sort.length];
        for (int i = 0; i < sort.length; i++) {
            String[] parts = sort[i].split(",");
            String property = parts[0];
            String direction = parts.length > 1 ? parts[1] : "asc";
            
            orders[i] = "desc".equalsIgnoreCase(direction) 
                ? Sort.Order.desc(property)
                : Sort.Order.asc(property);
        }
        
        return Sort.by(orders);
    }
}
