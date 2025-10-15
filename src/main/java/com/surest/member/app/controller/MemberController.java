package com.surest.member.app.controller;

import com.surest.member.app.dto.MemberRequestDTO;
import com.surest.member.app.dto.MemberResponseDTO;
import com.surest.member.app.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
@Slf4j
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


    // Accessible only by ADMIN
    @PostMapping
    public ResponseEntity<MemberResponseDTO> createMember(@Valid @RequestBody MemberRequestDTO request) {
        log.info("Received request to create member: {}", request);
        MemberResponseDTO response = memberService.createMember(request);
        log.info("Successfully created member with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping
    public ResponseEntity<Map<String, Object>> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName) {

        // Treat empty strings as null
        firstName = (firstName != null && !firstName.isBlank()) ? firstName : null;
        lastName = (lastName != null && !lastName.isBlank()) ? lastName : null;

        Page<MemberResponseDTO> membersPage = memberService.getAllMembers(page, size, sort, firstName, lastName);

        Map<String, Object> response = new HashMap<>();
        response.put("content", membersPage.getContent());
        response.put("currentPage", membersPage.getNumber());
        response.put("totalItems", membersPage.getTotalElements());
        response.put("totalPages", membersPage.getTotalPages());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public MemberResponseDTO getMemberById(@PathVariable UUID id) {
        return memberService.getMemberById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMember(@PathVariable UUID id) {
        memberService.deleteMember(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Member deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> updateMember(
            @PathVariable UUID id,
            @Valid @RequestBody MemberRequestDTO request) {

        MemberResponseDTO updatedMember = memberService.updateMember(id, request);
        return ResponseEntity.ok(updatedMember);
    }


}

