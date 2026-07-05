package com.yasirkhan.auth.repository;

import com.yasirkhan.auth.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Override
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.refreshToken")
    List<User> findAll();

    // CHANGED: overrode the inherited findById with an explicit LEFT JOIN FETCH.
    // Without this, every findById() call (used in UserServiceImpl.updateUser/blockUser/getUserById)
    // triggers a second SELECT for the lazy refreshToken association - fixing the N+1 pattern
    // at the single-entity level too, not just for list queries.
    @Override
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.refreshToken WHERE u.id = :id")
    Optional<User> findById(@Param("id") UUID id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.refreshToken WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    // CHANGED: added LEFT JOIN FETCH here too, for consistency with findByUsername/findById
    // so callers (e.g. password reset flow) don't trigger a lazy-load query later.
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.refreshToken WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
}