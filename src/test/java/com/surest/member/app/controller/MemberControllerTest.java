package com.surest.member.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.surest.member.app.dto.MemberRequestDTO;
import com.surest.member.app.dto.MemberResponseDTO;
import com.surest.member.app.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MemberControllerTest {


    private MockMvc mockMvc;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private ObjectMapper objectMapper;

    public static final String BASE_URL = "/api/v1/members";

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())  // support Java 8 date/time
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void testGetAllMembers() throws Exception {
        Page<MemberResponseDTO> mockPage = getMembersDetails();

        lenient().when(memberService.getAllMembers(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(mockPage);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "")
                        .param("firstName", "")
                        .param("lastName", ""))
                .andDo(print()) // ✅ debug output to console
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].firstName").value("archana"));

        verify(memberService, times(1))
                .getAllMembers(anyInt(), anyInt(), any(), any(), any());
    }


    @Test
    void testCreateMemberSuccess() throws Exception {
        // Arrange
        MemberRequestDTO requestDTO = memberRequestData();
        MemberResponseDTO responseDTO = memberResponseData();
        when(memberService.createMember(any(MemberRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated()) // Expect 201
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Archana"))
                .andExpect(jsonPath("$.email").value("archanapujar@gmail.com"));
        verify(memberService, times(1)).createMember(any(MemberRequestDTO.class));
    }

    @Test
    void testGetMemberByIdSuccess() throws Exception {
        UUID memberId = UUID.randomUUID();
        MemberResponseDTO member = new MemberResponseDTO(
                memberId, "archana", "pujar", "archanapujar@gmail.com", LocalDate.parse("1995-06-07")
        );

        when(memberService.getMemberById(memberId)).thenReturn(member);

        mockMvc.perform(get("/api/v1/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(memberId.toString()))
                .andExpect(jsonPath("$.firstName").value("archana"))
                .andExpect(jsonPath("$.lastName").value("pujar"));
    }

    @Test
    void testDeleteMemberSuccess() throws Exception {
        UUID memberId = UUID.randomUUID();

        // No need to return anything from void method
        doNothing().when(memberService).deleteMember(memberId);

        mockMvc.perform(delete("/api/v1/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Member deleted successfully"));

        // Verify the service method was called
        verify(memberService, times(1)).deleteMember(memberId);
    }

    @Test
    void testUpdateMemberSuccess() throws Exception {
        UUID memberId = UUID.fromString("a1d8234f-8235-4548-8fe3-8028c9f90c69");
        MemberRequestDTO request = memberRequestData();
        MemberResponseDTO updatedMember = memberResponseData();

        when(memberService.updateMember(eq(memberId), any(MemberRequestDTO.class)))
                .thenReturn(updatedMember);

        mockMvc.perform(put("/api/v1/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Archana"))
                .andExpect(jsonPath("$.lastName").value("Pujar"))
                .andExpect(jsonPath("$.email").value("archanapujar@gmail.com"))
                .andExpect(jsonPath("$.dateOfBirth").value("1995-06-07"));

        verify(memberService, times(1)).updateMember(eq(memberId), any(MemberRequestDTO.class));
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

    private Page<MemberResponseDTO> getMembersDetails() {
        List<MemberResponseDTO> members = new ArrayList<>();
        members.add(new MemberResponseDTO(UUID.randomUUID(), "archana", "pujar", "archanapujar@gmail.com", LocalDate.parse("1995-06-07")));
        members.add(new MemberResponseDTO(UUID.randomUUID(), "Ridha", "Pujar", "ridha@gmail.com", LocalDate.parse("1995-06-07")));
        return new PageImpl<>(members);
    }

}
