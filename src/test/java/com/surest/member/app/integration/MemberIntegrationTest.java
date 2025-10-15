package com.surest.member.app.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.surest.member.app.dto.LoginRequestDTO;
import com.surest.member.app.dto.MemberRequestDTO;
import com.surest.member.app.entity.Member;
import com.surest.member.app.entity.Role;
import com.surest.member.app.entity.User;
import com.surest.member.app.exception.ResourceNotFoundException;
import com.surest.member.app.repository.MemberRepository;
import com.surest.member.app.repository.RoleRepository;
import com.surest.member.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberIntegrationTest {

    @LocalServerPort
     int port;

    @Autowired
     TestRestTemplate restTemplate;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    private String baseUrl;
    private String adminToken;
    private String userToken;

    /** ---------------- ROLE INITIALIZATION (Run Once) ---------------- */
    @BeforeAll
    static void initRoles(@Autowired RoleRepository roleRepository) {
        if (roleRepository.findByRoleName("ADMIN").isEmpty()) {
            Role admin = new Role();
            admin.setRoleName("ADMIN");
            roleRepository.save(admin);
        }

        if (roleRepository.findByRoleName("USER").isEmpty()) {
            Role user = new Role();
            user.setRoleName("USER");
            roleRepository.save(user);
        }
    }

    /** ---------------- COMMON SETUP (Run Before Each Test) ---------------- */
    @BeforeEach
    void setup() throws Exception {
        baseUrl = "http://localhost:" + port + "/api/v1/members";

        memberRepository.deleteAll();
        userRepository.deleteAll();

        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Role 'ADMIN' not found"));

        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role 'USER' not found"));

        // Create Admin User
        User admin = new User();
        admin.setUsername("Aditi");
        admin.setPasswordHash(passwordEncoder.encode("Aditi@123"));
        admin.setRole(adminRole);
        userRepository.save(admin);

        // Create Normal User
        User user = new User();
        user.setUsername("Aman");
        user.setPasswordHash(passwordEncoder.encode("Aman@123"));
        user.setRole(userRole);
        userRepository.save(user);

        // Login Admin
        LoginRequestDTO adminLogin = new LoginRequestDTO("Aditi", "Aditi@123");
        ResponseEntity<String> adminLoginResp = restTemplate.postForEntity(
                "http://localhost:" + port + "/auth/login",
                adminLogin,
                String.class
        );
        assertThat(adminLoginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        adminToken = objectMapper.readTree(adminLoginResp.getBody()).get("token").asText();

        // Login User
        LoginRequestDTO userLogin = new LoginRequestDTO("Aman", "Aman@123");
        ResponseEntity<String> userLoginResp = restTemplate.postForEntity(
                "http://localhost:" + port + "/auth/login",
                userLogin,
                String.class
        );
        assertThat(userLoginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        userToken = objectMapper.readTree(userLoginResp.getBody()).get("token").asText();
    }

    /** ---------------- CREATE MEMBER ---------------- */
    @Test
    void testCreateMemberAsAdminShouldReturn201() throws Exception {
        MemberRequestDTO memberRequest = getMemberRequestDTO();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(memberRequest), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("Archana");
        assertThat(memberRepository.findAll()).hasSize(1);
    }

    @Test
    void testCreateMemberAsUserShouldReturn403() throws Exception {
        MemberRequestDTO memberRequest = getMemberRequestDTO();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userToken);

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(memberRequest), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /** ---------------- GET MEMBER BY ID ---------------- */
    @Test
    void testGetMemberByIdAsAdminShouldReturn200() {
        Member member = memberRepository.save(getMemberRequestData());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + member.getId(),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(member.getFirstName(), member.getLastName(), member.getEmail());
    }

    @Test
    void testGetMemberByIdAsUserShouldReturn200() {
        Member member = memberRepository.save(getMemberRequestData());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + member.getId(),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    /** ---------------- DELETE MEMBER ---------------- */
    @Test
    void testDeleteMemberAsAdminShouldReturn200() {
        Member member = memberRepository.save(getMemberRequestData());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + member.getId(),
                HttpMethod.DELETE,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    /** ---------------- Helper Methods ---------------- */
    private MemberRequestDTO getMemberRequestDTO() {
        MemberRequestDTO memberRequestDTO = new MemberRequestDTO();
        memberRequestDTO.setFirstName("Archana");
        memberRequestDTO.setLastName("Pujar");
        memberRequestDTO.setEmail("archana@gmail.com");
        memberRequestDTO.setDateOfBirth(LocalDate.of(2000, 1, 1));
        return memberRequestDTO;
    }

    private Member getMemberRequestData() {
        Member member = new Member();
        member.setFirstName("Archana");
        member.setLastName("Pujar");
        member.setEmail("archana@gmail.com");
        member.setDateOfBirth(LocalDate.of(1990, 5, 10));
        return member;
    }
}
