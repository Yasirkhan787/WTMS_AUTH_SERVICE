package com.yasirkhan.auth.repository;

import com.yasirkhan.auth.models.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // Already join-fetches the owning user, avoiding a second query on token validation.
    @Query("SELECT r FROM RefreshToken r JOIN FETCH r.user WHERE r.token = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.token = :token")
    void deleteByToken(@Param("token") String token);

}