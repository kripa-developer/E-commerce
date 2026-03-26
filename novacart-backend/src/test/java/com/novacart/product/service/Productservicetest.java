package com.novacart.product.service;

import com.novacart.category.domain.Category;
import com.novacart.category.repository.CategoryRepository;
import com.novacart.product.domain.Product;
import com.novacart.product.domain.ProductStatus;
import com.novacart.product.dto.ProductRequest;
import com.novacart.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category("Electronics", "electronics", null, null, null, 0);
        product = new Product("iPhone 17", "iphone-17", "desc", "short",
                new BigDecimal("112000"), new BigDecimal("114000"),
                "Apple", 10, category, "SKU001", 200);
    }

    // ════════════════════════════════════════════════
    // GET BY SLUG
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("getProductBySlug()")
    class GetProductBySlug {

        @Test
        @DisplayName("should return product when slug exists")
        void shouldReturnProductWhenSlugExists() {
            when(productRepository.findBySlug("iphone-17")).thenReturn(Optional.of(product));

            var result = productService.getProductBySlug("iphone-17");

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("iPhone 17");
        }

        @Test
        @DisplayName("should throw 404 when slug not found")
        void shouldThrow404WhenSlugNotFound() {
            when(productRepository.findBySlug("unknown-slug")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> productService.getProductBySlug("unknown-slug"));

            assertThat(ex.getStatusCode().value()).isEqualTo(404);
            assertThat(ex.getReason()).isEqualTo("Product not found");
        }
    }

    // ════════════════════════════════════════════════
    // GET BY ID
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("getProductById()")
    class GetProductById {

        @Test
        @DisplayName("should return product when id exists")
        void shouldReturnProductWhenIdExists() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            var result = productService.getProductById(1L);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("should throw 404 when id not found")
        void shouldThrow404WhenIdNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> productService.getProductById(99L));

            assertThat(ex.getStatusCode().value()).isEqualTo(404);
        }
    }

    // ════════════════════════════════════════════════
    // CREATE PRODUCT
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("createProduct()")
    class CreateProduct {

        private ProductRequest buildRequest(String name, String slug, String sku) {
            return new ProductRequest(name, slug, "desc", "short",
                    new BigDecimal("112000"), new BigDecimal("114000"),
                    "Apple", 10, 1L, sku, 200, null, null);
        }

        @Test
        @DisplayName("should create product with provided slug")
        void shouldCreateProductWithProvidedSlug() {
            ProductRequest request = buildRequest("iPhone 17", "iphone-17-pro", "SKU001");
            when(productRepository.existsBySlug("iphone-17-pro")).thenReturn(false);
            when(productRepository.existsBySku("SKU001")).thenReturn(false);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            var result = productService.createProduct(request);

            assertThat(result).isNotNull();
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("should auto-generate slug from name when slug is blank")
        void shouldAutoGenerateSlugFromName() {
            ProductRequest request = buildRequest("iPhone 17 Pro Max", "", "SKU002");
            when(productRepository.existsBySlug("iphone-17-pro-max")).thenReturn(false);
            when(productRepository.existsBySku("SKU002")).thenReturn(false);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            productService.createProduct(request);

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(captor.capture());
            assertThat(captor.getValue().getSlug()).isEqualTo("iphone-17-pro-max");
        }

        @Test
        @DisplayName("should append counter when slug already exists")
        void shouldAppendCounterWhenSlugExists() {
            ProductRequest request = buildRequest("iPhone 17", "iphone-17", "SKU003");
            when(productRepository.existsBySlug("iphone-17")).thenReturn(true);
            when(productRepository.existsBySlug("iphone-17-1")).thenReturn(false);
            when(productRepository.existsBySku("SKU003")).thenReturn(false);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            productService.createProduct(request);

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(captor.capture());
            assertThat(captor.getValue().getSlug()).isEqualTo("iphone-17-1");
        }

        @Test
        @DisplayName("should throw 409 when SKU already exists")
        void shouldThrow409WhenSkuExists() {
            ProductRequest request = buildRequest("iPhone 17", "iphone-17", "DUPLICATE-SKU");
            when(productRepository.existsBySlug("iphone-17")).thenReturn(false);
            when(productRepository.existsBySku("DUPLICATE-SKU")).thenReturn(true);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> productService.createProduct(request));

            assertThat(ex.getStatusCode().value()).isEqualTo(409);
            assertThat(ex.getReason()).isEqualTo("SKU already exists");
        }

        @Test
        @DisplayName("should throw 404 when category not found")
        void shouldThrow404WhenCategoryNotFound() {
            ProductRequest request = buildRequest("iPhone 17", "iphone-17", "SKU001");
            when(productRepository.existsBySlug("iphone-17")).thenReturn(false);
            when(productRepository.existsBySku("SKU001")).thenReturn(false);
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> productService.createProduct(request));

            assertThat(ex.getStatusCode().value()).isEqualTo(404);
            assertThat(ex.getReason()).isEqualTo("Category not found");
        }
    }

    // ════════════════════════════════════════════════
    // DELETE PRODUCT
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProduct {

        @Test
        @DisplayName("should set status to DISCONTINUED instead of deleting")
        void shouldSetStatusToDiscontinued() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            productService.deleteProduct(1L);

            assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("should throw 404 when product not found")
        void shouldThrow404WhenProductNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> productService.deleteProduct(99L));

            assertThat(ex.getStatusCode().value()).isEqualTo(404);
        }
    }

    // ════════════════════════════════════════════════
    // UPDATE STATUS
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("updateProductStatus()")
    class UpdateProductStatus {

        @Test
        @DisplayName("should update product status")
        void shouldUpdateProductStatus() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            productService.updateProductStatus(1L, ProductStatus.INACTIVE);

            assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("should throw 404 when product not found")
        void shouldThrow404WhenProductNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> productService.updateProductStatus(99L, ProductStatus.INACTIVE));

            assertThat(ex.getStatusCode().value()).isEqualTo(404);
        }
    }

    // ════════════════════════════════════════════════
    // SEARCH PRODUCTS
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("searchProducts()")
    class SearchProducts {

        @Test
        @DisplayName("should return page of product summaries")
        void shouldReturnPageOfProductSummaries() {
            when(productRepository.searchProducts(any(), any(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(product)));

            var result = productService.searchProducts(null, null, null, null, "iphone", 0, 10, "newest");

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return empty page when no matches")
        void shouldReturnEmptyPageWhenNoMatches() {
            when(productRepository.searchProducts(any(), any(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            var result = productService.searchProducts(null, null, null, null, "xyz123", 0, 10, "newest");

            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }
}