package com.surest.member.app.service;
import com.surest.member.app.dto.MemberRequestDTO;
import com.surest.member.app.dto.MemberResponseDTO;
import com.surest.member.app.entity.Member;
import com.surest.member.app.exception.ResourceNotFoundException;
import com.surest.member.app.repository.MemberRepository;
import jakarta.persistence.criteria.Predicate;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository, ModelMapper modelMapper) {
        this.memberRepository = memberRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public MemberResponseDTO createMember(MemberRequestDTO request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        Member member = new Member();
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail());
        member.setDateOfBirth(request.getDateOfBirth());

        Member savedMember = memberRepository.save(member);

        // Map Entity → ResponseDTO manually
        MemberResponseDTO response = new MemberResponseDTO();
        response.setId(savedMember.getId());
        response.setFirstName(savedMember.getFirstName());
        response.setLastName(savedMember.getLastName());
        response.setEmail(savedMember.getEmail());
        response.setDateOfBirth(savedMember.getDateOfBirth());
        return response;

    }


    @Override
    public Page<MemberResponseDTO> getAllMembers(int page, int size, String sortStr, String firstName, String lastName) {
        Sort sort = buildSort(sortStr);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Member> memberPage = hasFilters(firstName, lastName)
                ? getFilteredMembers(firstName, lastName, pageable)
                : memberRepository.findAll(pageable);

        return memberPage.map(member -> modelMapper.map(member, MemberResponseDTO.class));    }


    @Cacheable(value = "members", key = "#id")
    public MemberResponseDTO getMemberById(UUID id) {
        log.info("Fetching from DB for ID: {}", id);

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));
        return modelMapper.map(member, MemberResponseDTO.class);
    }

    @Override
    @CacheEvict(value = "members", key = "#id")
    public MemberResponseDTO updateMember(UUID id, MemberRequestDTO request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail());

        Member updatedMember = memberRepository.save(member);
        return modelMapper.map(updatedMember, MemberResponseDTO.class);
    }


    @Override
    @CacheEvict(value = "members", key = "#id")
    public void deleteMember(UUID id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));
        memberRepository.delete(member);
    }

    private Sort buildSort(String sortStr) {
        if (sortStr == null || sortStr.isBlank()) {
            return Sort.unsorted();
        }

        String[] sortParams = sortStr.split(",");
        if (sortParams.length == 2) {
            return Sort.by(Sort.Direction.fromString(sortParams[1].trim()), sortParams[0].trim());
        }

        return Sort.by(sortStr.trim());
    }

    private boolean hasFilters(String firstName, String lastName) {
        return (firstName != null && !firstName.isBlank())
                || (lastName != null && !lastName.isBlank());
    }

    private Page<Member> getFilteredMembers(String firstName, String lastName, Pageable pageable) {
        Specification<Member> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (firstName != null && !firstName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }
            if (lastName != null && !lastName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Member> memberPage = memberRepository.findAll(spec, pageable);

        if (memberPage.isEmpty()) {
            throw new ResourceNotFoundException("No members found for given search criteria");
        }

        return memberPage;
    }

}