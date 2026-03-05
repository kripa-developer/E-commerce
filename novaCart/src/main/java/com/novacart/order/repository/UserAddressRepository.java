package com.novacart.order.repository;

import com.novacart.order.domain.UserAddress;
import com.novacart.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserOrderByDefaultAddressDescCreatedAtDesc(User user);
    Optional<UserAddress> findByIdAndUser(Long id, User user);
    Optional<UserAddress> findByUserAndDefaultAddressTrue(User user);
    int countByUser(User user);
}
