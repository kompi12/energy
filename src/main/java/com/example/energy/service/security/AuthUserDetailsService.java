package com.example.energy.service.security;

import com.example.energy.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import java.util.Arrays;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final AppUserRepository repo;

    public AuthUserDetailsService(AppUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = repo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var auths = Arrays.stream(u.getRoles().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .toList();

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPasswordHash())
                .authorities(auths)
                .disabled(Boolean.FALSE.equals(u.getEnabled()))
                .build();
    }
}
