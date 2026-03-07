package com.yasirkhan.auth.repository;

import com.yasirkhan.auth.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
