package com.novacart.category.service;

import com.novacart.category.domain.Category;
import com.novacart.category.dto.CategoryRequest;
import com.novacart.category.dto.CategoryResponse;
import com.novacart.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // CategoryService.java
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(cat -> new CategoryResponse(
                        cat.getId(),
                        cat.getName(),
                        cat.getSlug(),
                        cat.getDescription(),
                        cat.getImageUrl(),
                        cat.getParent() != null ? cat.getParent().getId() : null,
                        cat.getParent() != null ? cat.getParent().getName() : null,
                        List.of(),   // ← flat list, no children here
                        cat.isActive(),
                        cat.getDisplayOrder()
                ))
                .toList();
    }
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        return categoryRepository.findAllRootCategories()   // WHERE parent_id IS NULL
                .stream()
                .filter(Category::isActive)
                .sorted(Comparator.comparingInt(Category::getDisplayOrder))
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));
        return CategoryResponse.from(category);
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new ResponseStatusException(CONFLICT, "Slug already exists");
        }
        if (categoryRepository.existsByName(request.name())) {
            throw new ResponseStatusException(CONFLICT, "Category name already exists");
        }

        Category parent = null;
        if (request.parentId() != null) {
            parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Parent category not found"));
        }

        Category category = new Category(
                request.name(), request.slug(), request.description(),
                request.imageUrl(), parent, request.displayOrder()
        );
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));

        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        category.setImageUrl(request.imageUrl());
        category.setDisplayOrder(request.displayOrder());

        if (request.parentId() != null) {
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));
        category.setActive(false);
        categoryRepository.save(category);
    }
}
