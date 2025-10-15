package com.surest.member.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDTO {
    private UUID id;// Database ID
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;  // Or LocalDate if you prefer


}
