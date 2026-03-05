package com.novacart.admin.service;

import com.novacart.admin.dto.DashboardStats;
import com.novacart.admin.dto.RecentOrderDto;
import com.novacart.order.domain.Order;
import com.novacart.order.domain.OrderStatus;
import com.novacart.order.dto.OrderResponse;
import com.novacart.order.repository.OrderRepository;
import com.novacart.product.repository.ProductRepository;
import com.novacart.review.domain.ReviewStatus;
import com.novacart.review.dto.ReviewResponse;
import com.novacart.review.repository.ReviewRepository;
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

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public AdminService(UserRepository userRepository, ProductRepository productRepository,
                        OrderRepository orderRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        long activeOrders = orderRepository.countActiveOrders();

        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant monthStart = Instant.now().minus(30, ChronoUnit.DAYS);

        BigDecimal revenueToday = safeRevenue(orderRepository.sumRevenueByDateRange(today, Instant.now()));
        BigDecimal revenueMonth = safeRevenue(orderRepository.sumRevenueByDateRange(monthStart, Instant.now()));

        List<RecentOrderDto> recentOrders = orderRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10))
                .stream().map(RecentOrderDto::from).toList();

        return new DashboardStats(totalUsers, totalProducts, totalOrders, activeOrders,
                revenueToday, revenueMonth, recentOrders);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(OrderStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = status != null
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        return orders.map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
        order.updateStatus(newStatus);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse markOrderPaid(Long orderId, String paymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
        order.markPaymentPaid(paymentId);
        return OrderResponse.from(orderRepository.save(order));
    }

    public Page<ReviewResponse> getAllReviews(String status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null) {
            ReviewStatus reviewStatus = ReviewStatus.valueOf(status);
            return reviewRepository.findByStatus(reviewStatus, pageable).map(ReviewResponse::from);
        }
        return reviewRepository.findAll(pageable).map(ReviewResponse::from);
    }

    @Transactional
    public ReviewResponse moderateReview(Long reviewId, String status) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Review not found"));
        review.setStatus(ReviewStatus.valueOf(status));
        return ReviewResponse.from(reviewRepository.save(review));
    }

    private BigDecimal safeRevenue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
