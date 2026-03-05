package com.novacart.product.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "product_attributes")
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String value;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected ProductAttribute() {}

    public ProductAttribute(Product product, String name, String value, int displayOrder) {
        this.product = product;
        this.name = name;
        this.value = value;
        this.displayOrder = displayOrder;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getName() { return name; }
    public String getValue() { return value; }
    public int getDisplayOrder() { return displayOrder; }

    public void setName(String name) { this.name = name; }
    public void setValue(String value) { this.value = value; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
