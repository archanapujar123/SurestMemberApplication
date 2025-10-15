package com.surest.member.app.controller;

import com.surest.member.app.dto.MemberRequestDTO;
import com.surest.member.app.dto.MemberResponseDTO;
import com.surest.member.app.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;
    private MemberController memberController;


    @BeforeEach
    void setUp() {
        memberService = mock(MemberService.class);
        memberController = new MemberController(memberService);
    }

    // ---------------- Create Member ----------------
    @Test
    void testCreateMemberSuccess() {
        MemberRequestDTO memberRequestDTO = memberRequestData();
        MemberResponseDTO memberResponseDTO = memberResponseData();
        when(memberService.createMember(memberRequestDTO)).thenReturn(memberResponseDTO);

        ResponseEntity<MemberResponseDTO> response = memberController.createMember(memberRequestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(memberResponseDTO);
        assertThat(response.getBody())
                .hasFieldOrPropertyWithValue("email", "archanapujar@gmail.com");
        verify(memberService, times(1)).createMember(memberRequestDTO);
    }

    // ---------------- Get Member by ID ----------------
    @Test
    void testGetMemberByIdSuccess() {
        UUID memberId = UUID.randomUUID();
        MemberResponseDTO memberResponseDTO = memberResponseData();
        when(memberService.getMemberById(memberId)).thenReturn(memberResponseDTO);

        MemberResponseDTO result = memberController.getMemberById(memberId);

        assertThat(result).isEqualTo(memberResponseDTO);
        assertThat(result.getEmail()).isEqualTo("archanapujar@gmail.com");
        verify(memberService, times(1)).getMemberById(memberId);
    }

    // ---------------- Get All Members ----------------
    @Test
    void testGetAllMembersSuccess() {
        List<MemberResponseDTO> members = new ArrayList<>();
        members.add(new MemberResponseDTO(UUID.randomUUID(), "Archana", "Pujar", "archanapujar@gmail.com", LocalDate.parse("1995-06-07")));
        members.add(new MemberResponseDTO(UUID.randomUUID(), "Ridha", "Pujar", "ridha@gmail.com", LocalDate.parse("1995-06-07")));

        Map<String, Object> mockResponse = new HashMap<>();
        Page<MemberResponseDTO> page = new PageImpl<>(members);

        mockResponse.put("content", members);
        mockResponse.put("totalElements", members.size());
        mockResponse.put("totalPages", 1);
        mockResponse.put("pageNumber", 0);
        mockResponse.put("pageSize", 10);

        // Mock service behavior
        when(memberService.getAllMembers(0, 10, "", "", "")).thenReturn(page);

        // Call controller directly
        ResponseEntity<Map<String, Object>> result = memberController.getMembers(0, 10, "", "", "");

        // Assertions
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().get("content")).isInstanceOf(List.class);
        assertThat(((List<?>) mockResponse.get("content"))).hasSize(2);


        @SuppressWarnings("unchecked")
        List<MemberResponseDTO> content = (List<MemberResponseDTO>) result.getBody().get("content");
        assertThat(content).hasSize(2);
        assertThat(content.get(0).getFirstName()).isEqualTo("Archana");
        assertThat(content.get(1).getEmail()).isEqualTo("ridha@gmail.com");

        assertThat(result.getBody())
                .containsEntry("totalElements", 2L);
        assertThat(result.getBody())
                .containsEntry("pageNumber", 0);


        // Verify service was called once
        verify(memberService, times(1)).getAllMembers(0, 10, "", "", "");
    }

    // ---------------- Update Member ----------------
    @Test
    void testUpdateMemberSuccess() {
        UUID memberId = UUID.randomUUID();
        MemberRequestDTO memberRequestDTO = memberRequestData();
        MemberResponseDTO memberResponseDTO = memberResponseData();

        when(memberService.updateMember(eq(memberId), any(MemberRequestDTO.class))).thenReturn(memberResponseDTO);

        ResponseEntity<MemberResponseDTO> response = memberController.updateMember(memberId, memberRequestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(memberResponseDTO);
        verify(memberService, times(1)).updateMember(memberId, memberRequestDTO);
    }

    // ---------------- Delete Member ----------------
    @Test
    void testDeleteMemberByIdSuccess() {
        UUID memberId = UUID.randomUUID();

        doNothing().when(memberService).deleteMember(memberId);

        ResponseEntity<Map<String, String>> response = memberController.deleteMemberById(memberId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsEntry("message", "Member deleted successfully");
        verify(memberService, times(1)).deleteMember(memberId);
    }



public MemberRequestDTO memberRequestData() {
        return new MemberRequestDTO(
                "Archana",
                "Pujar",
                "archanapujar@gmail.com",
                LocalDate.parse("1995-06-07")        );
    }

    public MemberResponseDTO memberResponseData() {
        return new MemberResponseDTO(
                UUID.randomUUID(),
                "Archana",
                "Pujar",
                "archanapujar@gmail.com",
                LocalDate.parse("1995-06-07")        );
    }


}
