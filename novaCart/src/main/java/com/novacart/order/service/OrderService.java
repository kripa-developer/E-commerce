package com.novacart.order.service;

import com.novacart.cart.domain.Cart;
import com.novacart.cart.repository.CartRepository;
import com.novacart.order.domain.*;
import com.novacart.order.dto.*;
import com.novacart.order.repository.OrderRepository;
import com.novacart.order.repository.UserAddressRepository;
import com.novacart.product.repository.ProductRepository;
import com.novacart.user.domain.User;
import com.novacart.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
                        UserAddressRepository addressRepository, UserRepository userRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse placeOrder(String email, PlaceOrderRequest request) {
        User user = getUser(email);

        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Cart is empty");
        }

        // Resolve shipping address
        ShippingAddress shippingAddress = resolveShippingAddress(user, request);

        // Validate stock and compute totals
        BigDecimal subtotal = BigDecimal.ZERO;
        for (var item : cart.getItems()) {
            var product = item.getProduct();
            if (!product.isInStock() || product.getStockQuantity() < item.getQuantity()) {
                throw new ResponseStatusException(BAD_REQUEST, "Insufficient stock for: " + product.getName());
            }
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal shippingCharge = subtotal.compareTo(BigDecimal.valueOf(499)) >= 0
                ? BigDecimal.ZERO : BigDecimal.valueOf(49);

        String orderNumber = generateOrderNumber();

        Order order = new Order(orderNumber, user, shippingAddress,
                subtotal, shippingCharge, BigDecimal.ZERO, request.paymentMethod());
        order.setNotes(request.notes());
        order.setExpectedDeliveryDate(Instant.now().plus(5, ChronoUnit.DAYS));

        // Add order items and deduct stock
        for (var cartItem : cart.getItems()) {
            order.getItems().add(new OrderItem(order, cartItem.getProduct(), cartItem.getQuantity()));
            cartItem.getProduct().decreaseStock(cartItem.getQuantity());
            productRepository.save(cartItem.getProduct());
        }

        Order saved = orderRepository.save(order);

        // Clear cart
        cart.getItems().clear();
        cart.touch();
        cartRepository.save(cart);

        return OrderResponse.from(saved);
    }

    public Page<OrderResponse> getMyOrders(String email, int page, int size) {
        User user = getUser(email);
        return orderRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size))
                .map(OrderResponse::from);
    }

    public OrderResponse getMyOrder(String email, Long orderId) {
        User user = getUser(email);
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId, String reason) {
        User user = getUser(email);
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));

        try {
            order.cancel(reason);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
        }

        // Restore stock
        for (var item : order.getItems()) {
            item.getProduct().increaseStock(item.getQuantity());
            productRepository.save(item.getProduct());
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    // --- Address management ---

    public List<AddressResponse> getMyAddresses(String email) {
        User user = getUser(email);
        return addressRepository.findByUserOrderByDefaultAddressDescCreatedAtDesc(user)
                .stream().map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse addAddress(String email, AddressRequest request) {
        User user = getUser(email);

        if (request.defaultAddress()) {
            addressRepository.findByUserAndDefaultAddressTrue(user)
                    .ifPresent(a -> { a.setDefaultAddress(false); addressRepository.save(a); });
        }

        UserAddress address = new UserAddress(user, request.name(), request.phone(),
                request.line1(), request.line2(), request.city(), request.state(),
                request.pincode(), request.country() != null ? request.country() : "India",
                request.addressType(), request.defaultAddress());

        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(String email, Long addressId, AddressRequest request) {
        User user = getUser(email);
        UserAddress address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Address not found"));

        if (request.defaultAddress() && !address.isDefaultAddress()) {
            addressRepository.findByUserAndDefaultAddressTrue(user)
                    .ifPresent(a -> { a.setDefaultAddress(false); addressRepository.save(a); });
        }

        address.setName(request.name());
        address.setPhone(request.phone());
        address.setLine1(request.line1());
        address.setLine2(request.line2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPincode(request.pincode());
        address.setCountry(request.country() != null ? request.country() : "India");
        address.setAddressType(request.addressType());
        address.setDefaultAddress(request.defaultAddress());

        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = getUser(email);
        UserAddress address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Address not found"));
        addressRepository.delete(address);
    }

    private ShippingAddress resolveShippingAddress(User user, PlaceOrderRequest request) {
        if (request.addressId() != null) {
            UserAddress saved = addressRepository.findByIdAndUser(request.addressId(), user)
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Address not found"));
            return saved.toShippingAddress();
        }

        if (request.newAddress() != null) {
            var a = request.newAddress();
            if (a.saveAddress()) {
                addAddress(user.getEmail(), new AddressRequest(
                        a.name(), a.phone(), a.line1(), a.line2(), a.city(), a.state(),
                        a.pincode(), a.country(), a.addressType(), false
                ));
            }
            return new ShippingAddress(a.name(), a.phone(), a.line1(), a.line2(),
                    a.city(), a.state(), a.pincode(), a.country() != null ? a.country() : "India");
        }

        throw new ResponseStatusException(BAD_REQUEST, "Please provide a shipping address");
    }

    private String generateOrderNumber() {
        return "NC-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    }
}
