package com.novacart.order.web;

import com.novacart.order.dto.*;
import com.novacart.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ---- Orders ----

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> placeOrder(Authentication auth, @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(201).body(orderService.placeOrder(auth.getName(), request));
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getMyOrders(auth.getName(), page, size));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getMyOrder(Authentication auth, @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getMyOrder(auth.getName(), orderId));
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            Authentication auth,
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(orderService.cancelOrder(auth.getName(), orderId, reason));
    }

    // ---- Addresses ----

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponse>> getMyAddresses(Authentication auth) {
        return ResponseEntity.ok(orderService.getMyAddresses(auth.getName()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> addAddress(Authentication auth, @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(201).body(orderService.addAddress(auth.getName(), request));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(Authentication auth,
                                                          @PathVariable Long addressId,
                                                          @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(orderService.updateAddress(auth.getName(), addressId, request));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(Authentication auth, @PathVariable Long addressId) {
        orderService.deleteAddress(auth.getName(), addressId);
        return ResponseEntity.noContent().build();
    }
}
