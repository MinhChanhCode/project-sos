package com.sqc.sos.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    UUID id;
    @Column(unique = true, nullable = false)
    String username;

    @Column(nullable = false)
    String password;

    String fullName;
    String phone;
    String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles", // Intermediate table name
            joinColumns = @JoinColumn(name = "user_id"), // Foreign key column referencing User
            inverseJoinColumns = @JoinColumn(name = "role_id") // Foreign key column referencing Role
    )
    Set<Role> roles;
}