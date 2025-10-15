package com.surest.member.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.surest.member.app.auth.JwtUtil;
import com.surest.member.app.dto.LoginRequestDTO;
import com.surest.member.app.dto.RegisterRequestDTO;
import com.surest.member.app.entity.Role;
import com.surest.member.app.entity.User;
import com.surest.member.app.repository.RoleRepository;
import com.surest.member.app.repository.UserRepository;
import com.surest.member.app.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;



    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper))
                .build();

    }

    // -------------------- LOGIN TESTS --------------------

    @Test
    void testLoginSuccess() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("archana");
        request.setPassword("archana@123");

        Authentication authMock = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);

        UserDetails userMock = mock(UserDetails.class);
        when(userMock.getUsername()).thenReturn("archana");
        // Use thenAnswer to return authorities
        when(userMock.getAuthorities()).thenAnswer(invocation ->
                List.of(new SimpleGrantedAuthority("USER"))
        );
        when(userDetailsService.loadUserByUsername("archana"))
                .thenReturn(userMock);

        when(jwtUtil.generateToken(eq("archana"), anyList()))
                .thenReturn("fake-jwt-token");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));

        verify(userDetailsService, times(1)).loadUserByUsername("archana");
    }


    @Test
    void testLoginInvalidCredentials() {
        // Given
        LoginRequestDTO request = loginRequestDetails();
        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // When
        ResponseEntity<Object> response = authController.login(request);

        // Then
        assertEquals("Invalid username or password", response.getBody());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(jwtUtil, never()).generateToken(any(), anyList());
    }


    @Test
     void testRegisterSuccess() {
        RegisterRequestDTO request =registerRequestDetails();
        Role role = new Role();
        role.setRoleName("USER");

        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(Mockito.anyString())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encodedPassword");

        ResponseEntity<Object> response = authController.register(request);

        assertEquals("User registered successfully", response.getBody());

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("archana@123");
    }

    @Test
    void testRegisterUsernameAlreadyExists() {
        RegisterRequestDTO request = registerRequestDetails();

        when(userRepository.findByUsername(Mockito.anyString()))
                .thenReturn(Optional.of(new User()));

        ResponseEntity<Object> response = authController.register(request);

        assertEquals("Username already exists", response.getBody());

        verify(userRepository, never()).save(any());
        verify(roleRepository, never()).findByRoleName(any());
    }

    @Test
    void testRegisterRoleNotFound() {
        RegisterRequestDTO request =  registerRequestDetails();
        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(Mockito.anyString())).thenReturn(Optional.empty());

        ResponseEntity<Object> response = authController.register(request);

        assertEquals("Role not found", response.getBody());

        verify(userRepository, never()).save(any());
    }

    private LoginRequestDTO loginRequestDetails() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("archana");
        request.setPassword("archana@123");
        return request;
    }

    private RegisterRequestDTO registerRequestDetails() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("archana");
        request.setPassword("archana@123");
        request.setRoleName("ROLE_USER");
        return request;
    }
}
