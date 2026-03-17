package com.novacart.user.repository;

import com.novacart.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Updates password_hash directly — avoids needing a setter on the entity */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.passwordHash = :hash WHERE u.id = :id")
    void updatePasswordHash(@Param("id") Long id, @Param("hash") String hash);
}
