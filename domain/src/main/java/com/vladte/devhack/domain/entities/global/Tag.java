package com.vladte.devhack.domain.entities.global;

import com.vladte.devhack.domain.entities.BasicEntity;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLLTreeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"questions", "children", "parent"})
public class Tag extends BasicEntity {

    @Column(unique = true, nullable = false)
    private String name;

    @Column(length = 100)
    private String slug;

    @Column(name = "path", nullable = false)
    @Type(PostgreSQLLTreeType.class)
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Tag parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Tag> children = new HashSet<>();

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<InterviewQuestion> questions = new HashSet<>();

    @Transient
    private int answeredQuestions = 0;

    @Transient
    private double progressPercentage = 0.0;

    /**
     * Generate a URL-friendly slug from the tag name.
     * Converts to lowercase, replaces non-alphanumeric characters with underscores,
     * and prefixes with 't_' if it starts with a digit.
     *
     * @return the generated slug
     */
    public String generateSlug() {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        // Replace non-alphanumeric characters with underscores and convert to lowercase
        String slug = name.trim()
                .replaceAll("[^a-zA-Z0-9]+", "_")
                .toLowerCase();

        // Remove leading/trailing underscores
        slug = slug.replaceAll("^_+|_+$", "");

        // Prefix with 't_' if starts with a digit
        if (!slug.isEmpty() && Character.isDigit(slug.charAt(0))) {
            slug = "t_" + slug;
        }

        return slug;
    }

    /**
     * Get the current slug, generating it if not set.
     *
     * @return the slug
     */
    public String getSlug() {
        if (slug == null || slug.trim().isEmpty()) {
            return generateSlug();
        }
        return slug;
    }

    /**
     * Calculate the progress percentage for this tag.
     * Progress is defined as the number of answered questions divided by the total number of questions.
     *
     * @return the progress percentage (0-100)
     */
    public double calculateProgressPercentage() {
        int totalQuestions = questions.size();
        if (totalQuestions == 0) {
            return 0.0;
        }
        return (double) answeredQuestions / totalQuestions * 100.0;
    }

    /**
     * Update the progress metrics for this tag.
     * This method should be called after the answeredQuestions count is set.
     */
    public void updateProgress() {
        this.progressPercentage = calculateProgressPercentage();
    }

    /**
     * Generate the ltree path for this tag based on its parent hierarchy.
     *
     * @return the generated path
     */
    public String generatePath() {
        if (parent == null) {
            // Root tag - path is just the slug
            return getSlug();
        } else {
            // Child tag - path is parent path + current slug
            String parentPath = parent.getPath();
            if (parentPath == null || parentPath.trim().isEmpty()) {
                parentPath = parent.generatePath();
            }
            return parentPath + "." + getSlug();
        }
    }

    /**
     * Get the depth level of this tag in the hierarchy (0 for root).
     *
     * @return the depth level
     */
    public int getDepth() {
        if (path == null || path.trim().isEmpty()) {
            return parent == null ? 0 : parent.getDepth() + 1;
        }
        return path.split("\\.").length - 1;
    }

    /**
     * Check if this tag is a root tag (has no parent).
     *
     * @return true if root tag, false otherwise
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Check if this tag is a leaf tag (has no children).
     *
     * @return true if leaf tag, false otherwise
     */
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    /**
     * Get the root tag of this hierarchy.
     *
     * @return the root tag
     */
    public Tag getRoot() {
        if (isRoot()) {
            return this;
        }
        return parent.getRoot();
    }

    /**
     * Check if this tag is an ancestor of the given tag.
     *
     * @param tag the tag to check
     * @return true if this tag is an ancestor of the given tag
     */
    public boolean isAncestorOf(Tag tag) {
        if (tag == null || tag.getPath() == null || this.path == null) {
            return false;
        }
        return tag.getPath().startsWith(this.path + ".") || tag.getPath().equals(this.path);
    }

    /**
     * Check if this tag is a descendant of the given tag.
     *
     * @param tag the tag to check
     * @return true if this tag is a descendant of the given tag
     */
    public boolean isDescendantOf(Tag tag) {
        if (tag == null) {
            return false;
        }
        return tag.isAncestorOf(this);
    }

    /**
     * Update the slug based on the current name and ensure path consistency.
     */
    public void updateSlugAndPath() {
        this.slug = generateSlug();
        this.path = generatePath();
    }
}
