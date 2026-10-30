package com.careerconnect.service;

import com.careerconnect.entity.Skill;
import com.careerconnect.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Transactional
    public Set<Skill> resolve(Set<String> names) {
        Set<Skill> result = new HashSet<>();
        if (names == null) {
            return result;
        }
        Map<String, String> unique = new LinkedHashMap<>();
        for (String raw : names) {
            if (raw != null && !raw.isBlank()) {
                unique.putIfAbsent(raw.trim().toLowerCase(), raw.trim());
            }
        }
        for (String displayName : unique.values()) {
            Skill skill = skillRepository.findByNameIgnoreCase(displayName)
                    .orElseGet(() -> skillRepository.save(new Skill(displayName)));
            result.add(skill);
        }
        return result;
    }
}