package com.surest.member.app.serviceImpl;

import com.surest.member.app.dto.MemberRequestDTO;
import com.surest.member.app.dto.MemberResponseDTO;
import com.surest.member.app.entity.Member;
import com.surest.member.app.exception.ResourceNotFoundException;
import com.surest.member.app.repository.MemberRepository;
import com.surest.member.app.service.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MemberServiceImplTest {

    private MemberRepository repository;
    ModelMapper modelMapper;
    private MemberServiceImpl service;

    private Member member;
    private MemberRequestDTO memberRequestDTO;
    private MemberResponseDTO memberResponseDTO;

    @BeforeEach
    void setUp() {
        repository = mock(MemberRepository.class);
        modelMapper = new ModelMapper();
        service = new MemberServiceImpl(repository, modelMapper);

        member = new Member();
        member.setId(UUID.randomUUID());
        member.setFirstName("Archana");
        member.setLastName("Pujar");
        member.setEmail("archanapujar@gmail.com");
        member.setDateOfBirth(LocalDate.of(1995, 6, 7));

        memberRequestDTO = new MemberRequestDTO("Archana", "Pujar", "archanapujar@gmail.com", LocalDate.parse("1995-06-07"));
        memberResponseDTO = modelMapper.map(member, MemberResponseDTO.class);
    }

    // --------------------------------------------------------------------------------

    @Test
    void testCreateMemberSuccess() {
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.save(any(Member.class))).thenReturn(member);

        memberResponseDTO = service.createMember(memberRequestDTO);
        assertThat(memberResponseDTO.getEmail()).isEqualTo("archanapujar@gmail.com");
        verify(repository, times(1)).save(any(Member.class));
    }

    @Test
    void testCreateMemberEmailAlreadyExistsThrowsException() {
        when(repository.existsByEmail("archanapujar@gmail.com")).thenReturn(true);
        assertThatThrownBy(() -> service.createMember(memberRequestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
    }

    // --------------------------------------------------------------------------------

    @Test
    void testGetAllMembersNoFiltersReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> page = new PageImpl<>(List.of(member));

        when(repository.findAll(pageable)).thenReturn(page);

        Page<MemberResponseDTO> result = service.getAllMembers(0, 10, null, null, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Archana");
    }

    @Test
    void testGetAllMembersWithFiltersNoResultsThrowsExceptionTest() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThatThrownBy(() ->
                service.getAllMembers(0, 10, null, "Nonexistent", null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No members found");
    }

    @Test
    void testGetAllMembersFilterNoResults() {
        int page = 0, size = 2;
        String firstName = "NonExistent";

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.getAllMembers(page, size, null, firstName, null));

        assertEquals("No members found for given search criteria", ex.getMessage());
    }



    @Test
    void testGetMemberByIdSuccess() {
        UUID id = member.getId();
        when(repository.findById(id)).thenReturn(Optional.of(member));

        MemberResponseDTO result = service.getMemberById(id);

        assertThat(result.getFirstName()).isEqualTo("Archana");
        verify(repository).findById(id);
    }

    @Test
    void testGetMemberByIdNotFoundThrowsException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMemberById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Member not found");
    }

    // --------------------------------------------------------------------------------

    @Test
    void testDeleteMemberSuccess() {
        UUID id = member.getId();
        when(repository.findById(id)).thenReturn(Optional.of(member));

        service.deleteMember(id);

        verify(repository).delete(member);
    }

    @Test
    void testDeleteMemberNotFoundThrowsException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMember(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Member not found");
    }

    // --------------------------------------------------------------------------------

    @Test
    void testUpdateMemberSuccess() {
        UUID id = member.getId();
        when(repository.findById(id)).thenReturn(Optional.of(member));
        when(repository.save(any(Member.class))).thenReturn(member);

        MemberRequestDTO updateRequest = new MemberRequestDTO("Updated", "Pujar", "updated@gmail.com", LocalDate.parse("1995-06-07"));

        MemberResponseDTO result = service.updateMember(id, updateRequest);

        assertThat(result.getEmail()).isEqualTo("updated@gmail.com");
        verify(repository).save(any(Member.class));
    }

    @Test
    void testUpdateMemberNotFoundThrowsException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateMember(id, memberRequestDTO))
                .isInstanceOf(RuntimeException.class);
    }
}
