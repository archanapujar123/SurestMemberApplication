package com.surest.member.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "user", schema = "surest")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id; // Primary Key

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username; // Not Null, Unique

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // Not Null

    // Foreign Key → role(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
