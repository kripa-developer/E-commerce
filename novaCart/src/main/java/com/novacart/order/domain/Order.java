package com.novacart.order.domain;

import com.novacart.user.domain.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_id")
    private String paymentId;

    // Snapshot of shipping address
    @Column(name = "shipping_name", nullable = false)
    private String shippingName;

    @Column(name = "shipping_phone", nullable = false)
    private String shippingPhone;

    @Column(name = "shipping_line1", nullable = false)
    private String shippingLine1;

    @Column(name = "shipping_line2")
    private String shippingLine2;

    @Column(name = "shipping_city", nullable = false)
    private String shippingCity;

    @Column(name = "shipping_state", nullable = false)
    private String shippingState;

    @Column(name = "shipping_pincode", nullable = false)
    private String shippingPincode;

    @Column(name = "shipping_country", nullable = false)
    private String shippingCountry;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingCharge = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "cancelled_reason")
    private String cancelledReason;

    @Column(name = "expected_delivery_date")
    private Instant expectedDeliveryDate;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Order() {}

    public Order(String orderNumber, User user, ShippingAddress address,
                 BigDecimal subtotal, BigDecimal shippingCharge, BigDecimal discountAmount,
                 String paymentMethod) {
        this.orderNumber = orderNumber;
        this.user = user;
        this.subtotal = subtotal;
        this.shippingCharge = shippingCharge;
        this.discountAmount = discountAmount;
        this.totalAmount = subtotal.add(shippingCharge).subtract(discountAmount);
        this.paymentMethod = paymentMethod;
        this.shippingName = address.name();
        this.shippingPhone = address.phone();
        this.shippingLine1 = address.line1();
        this.shippingLine2 = address.line2();
        this.shippingCity = address.city();
        this.shippingState = address.state();
        this.shippingPincode = address.pincode();
        this.shippingCountry = address.country();
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
        if (newStatus == OrderStatus.DELIVERED) {
            this.deliveredAt = Instant.now();
        }
    }

    public void cancel(String reason) {
        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel an order that is already shipped or delivered");
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledReason = reason;
    }

    public void markPaymentPaid(String paymentId) {
        this.paymentId = paymentId;
        this.paymentStatus = PaymentStatus.PAID;
        if (this.status == OrderStatus.PENDING) {
            this.status = OrderStatus.CONFIRMED;
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public User getUser() { return user; }
    public List<OrderItem> getItems() { return items; }
    public OrderStatus getStatus() { return status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentId() { return paymentId; }
    public String getShippingName() { return shippingName; }
    public String getShippingPhone() { return shippingPhone; }
    public String getShippingLine1() { return shippingLine1; }
    public String getShippingLine2() { return shippingLine2; }
    public String getShippingCity() { return shippingCity; }
    public String getShippingState() { return shippingState; }
    public String getShippingPincode() { return shippingPincode; }
    public String getShippingCountry() { return shippingCountry; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getShippingCharge() { return shippingCharge; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getNotes() { return notes; }
    public String getCancelledReason() { return cancelledReason; }
    public Instant getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setExpectedDeliveryDate(Instant date) { this.expectedDeliveryDate = date; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
}
