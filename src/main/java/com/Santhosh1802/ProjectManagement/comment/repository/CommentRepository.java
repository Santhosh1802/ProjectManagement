package com.Santhosh1802.ProjectManagement.comment.repository;

import com.Santhosh1802.ProjectManagement.comment.entity.Comment;
import com.Santhosh1802.ProjectManagement.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"author", "replies", "replies.author"})
    Page<Comment> findByTaskAndParentCommentIsNullOrderByCreatedAtAsc(Task task, Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"author"})
    Page<Comment> findByTaskOrderByCreatedAtAsc(Task task, Pageable pageable);

    long countByTask(Task task);

    long countByTaskAndParentCommentIsNull(Task task);
}
