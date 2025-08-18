package org.badmintonchain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UsersEntity user;

    @Column(name = "token", columnDefinition = "TEXT",nullable = false, length = 512)
    private String refreshToken;
//
//    @Column(nullable = false)
//    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked = false;
}
