package com.alurberkas.service;

import com.alurberkas.model.User;
import com.alurberkas.model.enums.Role;
import com.alurberkas.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String nip) throws UsernameNotFoundException {
        User user = userRepository.findByNip(nip)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan: " + nip));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Akun tidak aktif: " + nip);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getNip(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    public User findByNip(String nip) {
        return userRepository.findByNip(nip).orElse(null);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    public List<User> findByRole(Role role) {
        return userRepository.findByRoleAndActiveTrue(role);
    }

    @Transactional
    public User createUser(String nip, String nama, String rawPassword, Role role, String noHp) {
        if (userRepository.existsByNip(nip)) {
            throw new IllegalArgumentException("NIP sudah terdaftar: " + nip);
        }

        User user = new User();
        user.setNip(nip);
        user.setNama(nama);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setNoHp(noHp);
        user.setActive(true);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, String nama, Role role, String noHp, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + id));

        user.setNama(nama);
        user.setRole(role);
        user.setNoHp(noHp);
        user.setActive(active);

        return userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long id, String newRawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + id));
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
    }

    @Transactional
    public void toggleActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + id));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public long countUsers() {
        return userRepository.countByActiveTrue();
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }
}
