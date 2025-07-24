package com.vladte.devhack.common.repository.global;

import com.vladte.devhack.entities.global.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    // Custom query method to find a tag by name
    Optional<Tag> findByName(String name);

    // Search method with pagination
    @Query("SELECT t FROM Tag t WHERE " +
            "(:query IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Tag> searchTags(@Param("query") String query, Pageable pageable);
}
