package com.surest.member.app.controller;

import com.surest.member.app.auth.JwtUtil;
import com.surest.member.app.dto.LoginRequestDTO;
import com.surest.member.app.dto.LoginResponseDTO;
import com.surest.member.app.dto.RegisterRequestDTO;
import com.surest.member.app.entity.Role;
import com.surest.member.app.entity.User;
import com.surest.member.app.repository.RoleRepository;
import com.surest.member.app.repository.UserRepository;
import com.surest.member.app.service.CustomUserDetailsService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.AuthenticationException;


import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          CustomUserDetailsService userDetailsService, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;


    }


    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequestDTO request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Load user details (includes roles)
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

            // Extract roles
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .toList();

            // Generate JWT with username + roles
            String token = jwtUtil.generateToken(userDetails.getUsername(), roles);

            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (AuthenticationException e) {

            return ResponseEntity.status(401)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Invalid username or password");
        }


    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequestDTO request) {
        try {
            // Check if username already exists
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body("Username already exists");
            }

            // Fetch role entity
            Role role = roleRepository.findByRoleName(request.getRoleName())
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            newUser.setRole(role);

            userRepository.save(newUser);

            return ResponseEntity.ok("User registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());


        }
    }
}