package com.novacart.order.repository;

import com.novacart.order.domain.Order;
import com.novacart.order.domain.OrderStatus;
import com.novacart.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByIdAndUser(Long id, User user);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status NOT IN ('CANCELLED', 'RETURNED')")
    long countActiveOrders();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.paymentStatus = 'PAID' AND o.createdAt BETWEEN :from AND :to")
    java.math.BigDecimal sumRevenueByDateRange(@Param("from") Instant from, @Param("to") Instant to);
}
