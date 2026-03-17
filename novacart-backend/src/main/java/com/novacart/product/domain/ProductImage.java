package com.novacart.product.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    protected ProductImage() {}

    public ProductImage(Product product, String imageUrl, String altText, int displayOrder, boolean primary) {
        this.product = product;
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.displayOrder = displayOrder;
        this.primary = primary;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getImageUrl() { return imageUrl; }
    public String getAltText() { return altText; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isPrimary() { return primary; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setAltText(String altText) { this.altText = altText; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public void setPrimary(boolean primary) { this.primary = primary; }
}
