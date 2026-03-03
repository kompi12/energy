package com.example.energy.service.security;

import com.example.energy.model.AppUser;
import com.example.energy.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapUsers {

    @Bean
    CommandLineRunner seedAdmin(AppUserRepository repo, PasswordEncoder enc) {
        return args -> {
            if (repo.findByUsernameIgnoreCase("admin").isEmpty()) {
                AppUser u = new AppUser();
                u.setUsername("admin");
                u.setPasswordHash(enc.encode("admin"));
                u.setRoles("ADMIN,USER");
                u.setEnabled(true);
                repo.save(u);
            }
        };
    }
}
