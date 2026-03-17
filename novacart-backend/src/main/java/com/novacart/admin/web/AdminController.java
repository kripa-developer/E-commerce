package com.novacart.admin.web;

import com.novacart.admin.dto.DashboardStats;
import com.novacart.admin.service.AdminService;
import com.novacart.order.domain.OrderStatus;
import com.novacart.order.dto.OrderResponse;
import com.novacart.review.dto.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ---- Order management ----

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllOrders(status, page, size));
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(adminService.updateOrderStatus(orderId, status));
    }

    @PatchMapping("/orders/{orderId}/payment")
    public ResponseEntity<OrderResponse> markOrderPaid(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.markOrderPaid(orderId, body.get("paymentId")));
    }

    // ---- Review moderation ----

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewResponse>> getAllReviews(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllReviews(status, page, size));
    }

    @PatchMapping("/reviews/{reviewId}/status")
    public ResponseEntity<ReviewResponse> moderateReview(
            @PathVariable Long reviewId,
            @RequestParam String status) {
        return ResponseEntity.ok(adminService.moderateReview(reviewId, status));
    }
}
