package com.surest.member.app.repository;

import com.surest.member.app.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    // Find role by its name, e.g., "USER" or "ADMIN"
    Optional<Role> findByRoleName(String roleName);
}
