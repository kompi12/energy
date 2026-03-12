package com.example.energy.controller.security;

import com.example.energy.repository.AppUserRepository;
import com.example.energy.security.jwt.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173"})
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final AppUserRepository userRepo;

    public AuthController(AuthenticationManager authManager, JwtService jwtService, AppUserRepository userRepo) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token) {}

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        var user = userRepo.findByUsernameIgnoreCase(req.username()).orElseThrow();
        String token = jwtService.generateToken(user.getUsername(), user.getRoles());

        return ResponseEntity.ok(new LoginResponse(token));
    }
}
