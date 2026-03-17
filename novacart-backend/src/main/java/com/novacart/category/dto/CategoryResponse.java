package com.novacart.category.dto;

import com.novacart.category.domain.Category;
import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String imageUrl,
        Long parentId,          // just the ID, not the full parent object
        String parentName,
        List<CategoryResponse> children,
        boolean active,
        int displayOrder
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getSlug(),
                c.getDescription(),
                c.getImageUrl(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getParent() != null ? c.getParent().getName() : null,
                c.getChildren() != null
                        ? c.getChildren().stream()
                        .filter(Category::isActive)
                        .map(child -> new CategoryResponse(
                                child.getId(), child.getName(), child.getSlug(),
                                child.getDescription(), child.getImageUrl(),
                                c.getId(), c.getName(),   // parent ref is just ID+name, no recursion
                                List.of(),                // children of children = empty list (max 2 levels)
                                child.isActive(), child.getDisplayOrder()
                        ))
                        .sorted(java.util.Comparator.comparingInt(CategoryResponse::displayOrder))
                        .toList()
                        : List.of(),
                c.isActive(),
                c.getDisplayOrder()
        );
    }
}