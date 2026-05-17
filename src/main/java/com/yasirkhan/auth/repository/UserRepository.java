package com.yasirkhan.auth.repository;

import com.yasirkhan.auth.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Override
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.refreshToken")
    List<User> findAll();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.refreshToken WHERE u.username = :username")
    Optional<User> findByUsername(String username);
}
