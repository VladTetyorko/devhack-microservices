package com.vladte.devhack.domain.repository.global;

import com.vladte.devhack.domain.entities.global.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    // Custom query method to find a tag by name
    Optional<Tag> findByName(String name);

    // Find tag by slug
    Optional<Tag> findBySlug(String slug);

    // Find tag by parent and slug combination
    Optional<Tag> findByParentAndSlug(Tag parent, String slug);

    // Find root tags (no parent)
    List<Tag> findByParentIsNull();

    // Find direct children of a tag
    List<Tag> findByParent(Tag parent);

    // Search method with pagination
    @Query("SELECT t FROM Tag t WHERE " +
            "(:query IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Tag> searchTags(@Param("query") String query, Pageable pageable);

    // Find all descendants of a tag using ltree path
    @Query(value = "SELECT * FROM tags WHERE path <@ :parentPath::ltree", nativeQuery = true)
    List<Tag> findDescendants(@Param("parentPath") String parentPath);

    // Find all ancestors of a tag using ltree path
    @Query(value = "SELECT * FROM tags WHERE :tagPath::ltree <@ path", nativeQuery = true)
    List<Tag> findAncestors(@Param("tagPath") String tagPath);

    // Find subtree limited by depth
    @Query(value = "SELECT * FROM tags WHERE path <@ :parentPath::ltree AND nlevel(path) <= nlevel(:parentPath::ltree) + :depth", nativeQuery = true)
    List<Tag> findSubtreeWithDepth(@Param("parentPath") String parentPath, @Param("depth") int depth);

    // Find direct parent by path
    @Query(value = "SELECT * FROM tags WHERE path = subpath(:childPath::ltree, 0, nlevel(:childPath::ltree) - 1)", nativeQuery = true)
    Optional<Tag> findParentByPath(@Param("childPath") String childPath);

    // Find tags at specific depth level
    @Query(value = "SELECT * FROM tags WHERE nlevel(path) = :depth", nativeQuery = true)
    List<Tag> findByDepth(@Param("depth") int depth);

    // Find siblings of a tag (same parent)
    @Query("SELECT t FROM Tag t WHERE t.parent = :parent AND t.id != :excludeId")
    List<Tag> findSiblings(@Param("parent") Tag parent, @Param("excludeId") UUID excludeId);

    // Update paths for subtree when moving a tag
    @Modifying
    @Query(value = "UPDATE tags SET path = :newPathPrefix::ltree || subpath(path, nlevel(:oldPathPrefix::ltree)) " +
            "WHERE path <@ :oldPathPrefix::ltree", nativeQuery = true)
    void updateSubtreePaths(@Param("oldPathPrefix") String oldPathPrefix, @Param("newPathPrefix") String newPathPrefix);

    // Check if a path exists (for cycle prevention)
    @Query(value = "SELECT COUNT(*) > 0 FROM tags WHERE path = :path::ltree", nativeQuery = true)
    boolean existsByPath(@Param("path") String path);

    // Find tags by path pattern
    @Query(value = "SELECT * FROM tags WHERE path ~ :pattern::lquery", nativeQuery = true)
    List<Tag> findByPathPattern(@Param("pattern") String pattern);
}
