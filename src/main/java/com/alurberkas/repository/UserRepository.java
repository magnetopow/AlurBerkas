package com.alurberkas.repository;

import com.alurberkas.model.User;
import com.alurberkas.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByNip(String nip);

    List<User> findByRole(Role role);

    List<User> findByRoleAndActiveTrue(Role role);

    List<User> findByActiveTrue();

    boolean existsByNip(String nip);

    long countByRole(Role role);

    long countByActiveTrue();
}
