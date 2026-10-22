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

    private GetCommentResponse toResponse(Comment comment) {
        User author = comment.getAuthor();
        CommentAuthorResponse authorDto = new CommentAuthorResponse(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getEmail()
        );

        return new GetCommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getContent(),
                authorDto,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    /**
     * Adds a comment to a task.
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

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setTask(task);
        comment.setAuthor(author);

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
     * Retrieves paginated comments for a task in chronological order.
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

        Page<Comment> commentPage = commentRepository.findByTaskOrderByCreatedAtAsc(
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
