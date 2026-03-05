package com.novacart.product.repository;

import com.novacart.product.domain.Product;
import com.novacart.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);
    boolean existsBySku(String sku);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status, Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.status = 'ACTIVE'
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:brand IS NULL OR LOWER(p.brand) = LOWER(:brand))
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:keyword IS NULL OR (
                LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ))
        """)
    Page<Product> searchProducts(
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category WHERE p.status = 'ACTIVE' ORDER BY p.soldCount DESC",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE p.status = 'ACTIVE'")
    Page<Product> findBestSellers(Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category WHERE p.status = 'ACTIVE' ORDER BY p.createdAt DESC",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE p.status = 'ACTIVE'")
    Page<Product> findNewArrivals(Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category WHERE p.status = 'ACTIVE' AND p.discountPercent IS NOT NULL ORDER BY p.discountPercent DESC",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE p.status = 'ACTIVE' AND p.discountPercent IS NOT NULL")
    Page<Product> findTopDeals(Pageable pageable);

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.status = 'ACTIVE' AND p.category.id = :categoryId ORDER BY p.brand ASC")
    java.util.List<String> findBrandsByCategoryId(@Param("categoryId") Long categoryId);
}
