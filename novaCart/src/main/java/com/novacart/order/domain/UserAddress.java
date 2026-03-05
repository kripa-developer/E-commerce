package com.novacart.order.domain;

import com.novacart.user.domain.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_addresses")
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(name = "line1", nullable = false)
    private String line1;

    @Column(name = "line2")
    private String line2;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private String country = "India";

    @Column(name = "address_type")
    private String addressType; // HOME, WORK, OTHER

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UserAddress() {}

    public UserAddress(User user, String name, String phone, String line1, String line2,
                       String city, String state, String pincode, String country,
                       String addressType, boolean defaultAddress) {
        this.user = user;
        this.name = name;
        this.phone = phone;
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.country = country;
        this.addressType = addressType;
        this.defaultAddress = defaultAddress;
    }

    @PrePersist
    void prePersist() { this.createdAt = Instant.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getLine1() { return line1; }
    public String getLine2() { return line2; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }
    public String getCountry() { return country; }
    public String getAddressType() { return addressType; }
    public boolean isDefaultAddress() { return defaultAddress; }
    public Instant getCreatedAt() { return createdAt; }

    public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLine1(String line1) { this.line1 = line1; }
    public void setLine2(String line2) { this.line2 = line2; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public void setCountry(String country) { this.country = country; }
    public void setAddressType(String addressType) { this.addressType = addressType; }

    public ShippingAddress toShippingAddress() {
        return new ShippingAddress(name, phone, line1, line2, city, state, pincode, country);
    }
}
