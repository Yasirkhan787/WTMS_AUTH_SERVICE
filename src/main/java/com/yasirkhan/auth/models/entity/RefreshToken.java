package com.yasirkhan.auth.models.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Date expirationDate;

    // CHANGED: fetch = LAZY (owning side, so a real proxy is used). Prevents this entity from
    // eagerly pulling the full User row whenever a RefreshToken is loaded on its own,
    // e.g. via validateRefreshToken()/deleteRefreshToken() where the user isn't needed.
    // CHANGED: excluded from toString/equals - with the field now LAZY, calling toString()
    // or equals() outside an active Hibernate session would otherwise throw
    // LazyInitializationException.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;
}