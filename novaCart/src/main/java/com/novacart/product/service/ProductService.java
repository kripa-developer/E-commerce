package com.novacart.product.service;

import com.novacart.category.domain.Category;
import com.novacart.category.repository.CategoryRepository;
import com.novacart.product.domain.Product;
import com.novacart.product.domain.ProductAttribute;
import com.novacart.product.domain.ProductImage;
import com.novacart.product.domain.ProductStatus;
import com.novacart.product.dto.ProductRequest;
import com.novacart.product.dto.ProductResponse;
import com.novacart.product.dto.ProductSummary;
import com.novacart.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    @Transactional(readOnly = true)
    public Page<ProductSummary> searchProducts(Long categoryId, String brand, BigDecimal minPrice,
                                                BigDecimal maxPrice, String keyword, int page, int size, String sortBy) {
        Sort sort = resolveSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.searchProducts(categoryId, brand, minPrice, maxPrice, keyword, pageable)
                .map(ProductSummary::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductSummary> getBestSellers(int page, int size) {
        return productRepository.findBestSellers(PageRequest.of(page, size)).map(ProductSummary::from);
    }

    public Page<ProductSummary> getNewArrivals(int page, int size) {
        return productRepository.findNewArrivals(PageRequest.of(page, size)).map(ProductSummary::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductSummary> getTopDeals(int page, int size) {
        return productRepository.findTopDeals(PageRequest.of(page, size)).map(ProductSummary::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<String> getBrandsByCategory(Long categoryId) {
        return productRepository.findBrandsByCategoryId(categoryId);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        // Auto-generate slug from name if not provided
        String slug = (request.slug() != null && !request.slug().isBlank())
                ? request.slug()
                : generateSlug(request.name());

        // Make slug unique
        String baseSlug = slug;
        int counter = 1;
        while (productRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        if (request.sku() != null && productRepository.existsBySku(request.sku())) {
            throw new ResponseStatusException(CONFLICT, "SKU already exists");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));

        Product product = new Product(
                request.name(),
                slug,                                                        // ← local variable, NOT request.slug()
                request.description(),
                request.shortDescription(),
                request.price(),
                request.mrp(),
                request.brand(),
                request.stockQuantity() != null ? request.stockQuantity() : 0,
                category,
                request.sku(),
                request.weightGrams()
        );

        if (request.images() != null) {
            for (ProductRequest.ImageRequest img : request.images()) {
                product.getImages().add(new ProductImage(
                        product, img.imageUrl(), img.altText(), img.displayOrder(), img.primary()
                ));
            }
        }

        if (request.attributes() != null) {
            for (ProductRequest.AttributeRequest attr : request.attributes()) {
                product.getAttributes().add(new ProductAttribute(
                        product, attr.name(), attr.value(), attr.displayOrder()
                ));
            }
        }

        return ProductResponse.from(productRepository.save(product));
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));

        // Auto-generate slug from name if not provided
        String slug = (request.slug() != null && !request.slug().isBlank())
                ? request.slug()
                : generateSlug(request.name());

        // Make slug unique — skip if slug hasn't changed from current product
        if (!slug.equals(product.getSlug())) {
            String baseSlug = slug;
            int counter = 1;
            while (productRepository.existsBySlug(slug)) {
                slug = baseSlug + "-" + counter++;
            }
        }

        product.setName(request.name());
        product.setSlug(slug);                                              // ← local variable, NOT request.slug()
        product.setDescription(request.description());
        product.setShortDescription(request.shortDescription());
        product.setPrice(request.price());
        product.setMrp(request.mrp());
        product.setBrand(request.brand());
        product.setStockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0);
        product.setCategory(category);
        product.setSku(request.sku());
        product.setWeightGrams(request.weightGrams());

        product.getImages().clear();
        if (request.images() != null) {
            for (ProductRequest.ImageRequest img : request.images()) {
                product.getImages().add(new ProductImage(
                        product, img.imageUrl(), img.altText(), img.displayOrder(), img.primary()
                ));
            }
        }

        product.getAttributes().clear();
        if (request.attributes() != null) {
            for (ProductRequest.AttributeRequest attr : request.attributes()) {
                product.getAttributes().add(new ProductAttribute(
                        product, attr.name(), attr.value(), attr.displayOrder()
                ));
            }
        }

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void updateProductStatus(Long id, ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        product.setStatus(status);
        productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);
    }

    private Sort resolveSort(String sortBy) {
        return switch (sortBy == null ? "relevance" : sortBy) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "rating" -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "popular" -> Sort.by(Sort.Direction.DESC, "soldCount");
            default -> Sort.by(Sort.Direction.DESC, "soldCount");
        };
    }
}
