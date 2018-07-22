package com.github.njuro.jboard.models;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a thread submitted to board
 *
 * @author njuro
 */
@Entity
@Table(name = "threads")
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Thread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    private String subject;

    @Basic
    private boolean locked;

    @Basic
    private boolean stickied;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @ManyToOne(targetEntity = Board.class, fetch = FetchType.EAGER)
    @EqualsAndHashCode.Include
    private Board board;

    @OneToMany(targetEntity = Post.class, mappedBy = "thread", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    @ToString.Exclude
    private List<Post> posts;

    @OneToOne(targetEntity = Post.class, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Include
    private Post originalPost;

    @PrePersist
    private void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getPostNumber() {
        return originalPost != null ? originalPost.getPostNumber() : null;
    }

    public void toggleLock() {
        this.locked = !this.locked;
    }

    public void toggleSticky() {
        this.stickied = !this.stickied;
    }
}