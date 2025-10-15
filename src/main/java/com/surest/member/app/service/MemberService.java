package com.surest.member.app.service;

import com.surest.member.app.dto.MemberRequestDTO;
import com.surest.member.app.dto.MemberResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MemberService {
    MemberResponseDTO createMember(MemberRequestDTO request);
    Page<MemberResponseDTO> getAllMembers(int page, int size, String sort, String firstName, String lastName);

    MemberResponseDTO getMemberById(UUID id);

    void deleteMember(UUID id);

    MemberResponseDTO updateMember(UUID id, @Valid MemberRequestDTO request);
}
