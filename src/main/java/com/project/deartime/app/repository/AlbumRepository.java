package com.project.deartime.app.repository;

import com.project.deartime.app.domain.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByUserIdOrderByCreatedAtDesc(Long userId);
}
