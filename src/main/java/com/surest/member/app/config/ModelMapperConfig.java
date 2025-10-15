package com.surest.member.app.config;

import com.surest.member.app.dto.MemberRequestDTO;
import com.surest.member.app.entity.Member;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // Convert String → LocalDate
        mapper.typeMap(MemberRequestDTO.class, Member.class)
                .addMappings(m -> m.using(ctx -> LocalDate.parse((String) ctx.getSource()))
                        .map(MemberRequestDTO::getDateOfBirth, Member::setDateOfBirth));

        return mapper;
    }
}
