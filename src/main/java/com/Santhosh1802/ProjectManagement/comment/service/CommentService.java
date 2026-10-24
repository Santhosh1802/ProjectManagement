package com.Santhosh1802.ProjectManagement.comment.service;

import com.Santhosh1802.ProjectManagement.comment.dto.request.CreateCommentRequest;
import com.Santhosh1802.ProjectManagement.comment.dto.response.CommentAuthorResponse;
import com.Santhosh1802.ProjectManagement.comment.dto.response.GetCommentResponse;
import com.Santhosh1802.ProjectManagement.comment.entity.Comment;
import com.Santhosh1802.ProjectManagement.comment.event.TaskCommentCreatedEvent;
import com.Santhosh1802.ProjectManagement.comment.repository.CommentRepository;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.task.TaskNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CommentService(
            CommentRepository commentRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            @Autowired(required = false) ApplicationEventPublisher eventPublisher
    ) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    public GetCommentResponse toResponse(Comment comment) {
        User author = comment.getAuthor();
        CommentAuthorResponse authorDto = new CommentAuthorResponse(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getEmail()
        );

        java.util.List<GetCommentResponse> replyDtos = null;
        try {
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                replyDtos = comment.getReplies().stream()
                        .map(this::toResponse)
                        .toList();
            }
        } catch (Exception e) {
            // In case lazy loading replies collection fails outside an active transaction
            replyDtos = java.util.Collections.emptyList();
        }

        String displayContent = comment.isDeleted() ? "This comment has been deleted." : comment.getContent();

        return new GetCommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                displayContent,
                authorDto,
                comment.isDeleted(),
                replyDtos,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    /**
     * Adds a comment or reply to a task.
     */
    @PreAuthorize("hasRole('ADMIN') or @commentAuthorization.canCommentOnTask(#request.taskId, authentication)")
    @Transactional
    public GetCommentResponse addComment(
            @P("request") CreateCommentRequest request,
            Authentication authentication
    ) {
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new TaskNotFoundException("Task of id: " + request.getTaskId() + " not found"));

        User author = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));

        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found: " + request.getParentCommentId()));

            // Cross-task reply validation
            if (!parentComment.getTask().getId().equals(task.getId())) {
                throw new IllegalArgumentException("Cannot reply to a comment belonging to a different task");
            }

            // Nesting policy: 1 reply level (Instagram style). If parent itself is a reply, flatten to the root parent.
            if (parentComment.getParentComment() != null) {
                parentComment = parentComment.getParentComment();
            }
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setParentComment(parentComment);
        comment.setDeleted(false);

        Comment saved = commentRepository.save(comment);

        if (eventPublisher != null) {
            UUID projectId = task.getProject() != null ? task.getProject().getId() : null;
            eventPublisher.publishEvent(TaskCommentCreatedEvent.of(
                    saved.getId(),
                    task.getId(),
                    projectId,
                    author.getId()
            ));
        }

        return toResponse(saved);
    }

    /**
     * Edits an existing comment.
     */
    @Transactional
    public GetCommentResponse editComment(
            UUID commentId,
            com.Santhosh1802.ProjectManagement.comment.dto.request.UpdateCommentRequest request,
            Authentication authentication
    ) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));

        if (comment.isDeleted()) {
            throw new IllegalArgumentException("Cannot edit a deleted comment");
        }

        // Author-only or Admin authorization
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !comment.getAuthor().getEmail().equals(authentication.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("You can only edit your own comments");
        }

        comment.setContent(request.getContent());
        Comment updated = commentRepository.save(comment);
        return toResponse(updated);
    }

    /**
     * Deletes a comment. If it has replies, performs soft deletion (tombstone) to preserve thread integrity.
     * Otherwise removes it directly.
     */
    @Transactional
    public void deleteComment(UUID commentId, Authentication authentication) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isAuthor = comment.getAuthor().getEmail().equals(authentication.getName());

        if (!isAdmin && !isAuthor) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to delete this comment");
        }

        boolean hasReplies = comment.getReplies() != null && !comment.getReplies().isEmpty();
        if (hasReplies) {
            comment.setDeleted(true);
            commentRepository.save(comment);
        } else {
            commentRepository.delete(comment);
        }
    }

    /**
     * Retrieves paginated top-level comments for a task in chronological order, with nested replies.
     */
    @PreAuthorize("hasRole('ADMIN') or @commentAuthorization.canCommentOnTask(#taskId, authentication)")
    public PageResponse<GetCommentResponse> getCommentsByTask(
            @P("taskId") UUID taskId,
            int page,
            int size,
            Authentication authentication
    ) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task of id: " + taskId + " not found"));

        Page<Comment> commentPage = commentRepository.findByTaskAndParentCommentIsNullOrderByCreatedAtAsc(
                task,
                PageRequest.of(page, size)
        );

        return new PageResponse<>(
                commentPage.getContent().stream().map(this::toResponse).toList(),
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.isFirst(),
                commentPage.isLast()
        );
    }
}
