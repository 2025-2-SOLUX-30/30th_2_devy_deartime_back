package com.project.deartime.app.letter.repository;

import com.project.deartime.app.domain.LetterTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LetterThemeRepository extends JpaRepository<LetterTheme, Long> {
    Optional<LetterTheme> findByCode(String code);
}
