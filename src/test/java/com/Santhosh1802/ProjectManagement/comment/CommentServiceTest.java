package com.Santhosh1802.ProjectManagement.comment;

import com.Santhosh1802.ProjectManagement.comment.dto.request.CreateCommentRequest;
import com.Santhosh1802.ProjectManagement.comment.dto.response.GetCommentResponse;
import com.Santhosh1802.ProjectManagement.comment.entity.Comment;
import com.Santhosh1802.ProjectManagement.comment.event.TaskCommentCreatedEvent;
import com.Santhosh1802.ProjectManagement.comment.repository.CommentRepository;
import com.Santhosh1802.ProjectManagement.comment.service.CommentService;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentService commentService;

    private UUID taskId;
    private UUID projectId;
    private UUID authorId;
    private UUID commentId;
    private Task task;
    private User author;
    private Project project;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        project = new Project();
        project.setId(projectId);

        author = new User();
        author.setId(authorId);
        author.setEmail("author@example.com");
        author.setFirstName("Alice");
        author.setLastName("Dev");

        task = new Task();
        task.setId(taskId);
        task.setTitle("Design comments architecture");
        task.setProject(project);
    }

    @Test
    @DisplayName("Should successfully add a comment and publish TaskCommentCreatedEvent")
    void testAddCommentSuccess() {
        CreateCommentRequest request = new CreateCommentRequest(taskId, "Looks good to me!");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(authentication.getName()).thenReturn("author@example.com");
        when(userRepository.findByEmail("author@example.com")).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(commentId);
            c.setCreatedAt(Instant.now());
            c.setUpdatedAt(Instant.now());
            return c;
        });

        GetCommentResponse response = commentService.addComment(request, authentication);

        assertNotNull(response);
        assertEquals(commentId, response.getId());
        assertEquals("Looks good to me!", response.getContent());
        assertEquals(taskId, response.getTaskId());
        assertEquals(authorId, response.getAuthor().getId());

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        Object published = eventCaptor.getValue();
        assertInstanceOf(TaskCommentCreatedEvent.class, published);
        TaskCommentCreatedEvent event = (TaskCommentCreatedEvent) published;
        assertEquals(commentId, event.commentId());
        assertEquals(taskId, event.taskId());
        assertEquals(projectId, event.projectId());
        assertEquals(authorId, event.authorId());
    }

    @Test
    @DisplayName("Should retrieve paginated comments for task with replies")
    void testGetCommentsByTask() {
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setContent("Hello World");
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        Page<Comment> page = new PageImpl<>(Collections.singletonList(comment), PageRequest.of(0, 10), 1);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.findByTaskAndParentCommentIsNullOrderByCreatedAtAsc(eq(task), any(PageRequest.class))).thenReturn(page);

        PageResponse<GetCommentResponse> response = commentService.getCommentsByTask(taskId, 0, 10, authentication);

        assertEquals(1, response.getContent().size());
        assertEquals("Hello World", response.getContent().get(0).getContent());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Should successfully add a reply to an existing comment")
    void testAddReplySuccess() {
        Comment parent = new Comment();
        parent.setId(commentId);
        parent.setTask(task);
        parent.setAuthor(author);

        UUID replyId = UUID.randomUUID();
        CreateCommentRequest request = new CreateCommentRequest(taskId, commentId, "This is a reply");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(parent));
        when(authentication.getName()).thenReturn("author@example.com");
        when(userRepository.findByEmail("author@example.com")).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(replyId);
            c.setCreatedAt(Instant.now());
            c.setUpdatedAt(Instant.now());
            return c;
        });

        GetCommentResponse response = commentService.addComment(request, authentication);

        assertNotNull(response);
        assertEquals(replyId, response.getId());
        assertEquals(commentId, response.getParentCommentId());
        assertEquals("This is a reply", response.getContent());
    }

    @Test
    @DisplayName("Should reject reply across different tasks")
    void testRejectCrossTaskReply() {
        Task differentTask = new Task();
        differentTask.setId(UUID.randomUUID());

        Comment parentOnOtherTask = new Comment();
        parentOnOtherTask.setId(commentId);
        parentOnOtherTask.setTask(differentTask);

        CreateCommentRequest request = new CreateCommentRequest(taskId, commentId, "Cross-task reply");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(parentOnOtherTask));
        when(authentication.getName()).thenReturn("author@example.com");
        when(userRepository.findByEmail("author@example.com")).thenReturn(Optional.of(author));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                commentService.addComment(request, authentication)
        );

        assertTrue(ex.getMessage().contains("Cannot reply to a comment belonging to a different task"));
    }

    @Test
    @DisplayName("Should soft-delete comment when replies exist")
    void testSoftDeleteCommentWithReplies() {
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setAuthor(author);
        comment.setTask(task);

        Comment reply = new Comment();
        reply.setId(UUID.randomUUID());
        comment.setReplies(java.util.List.of(reply));

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(authentication.getName()).thenReturn("author@example.com");
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

        commentService.deleteComment(commentId, authentication);

        assertTrue(comment.isDeleted());
        verify(commentRepository).save(comment);
        verify(commentRepository, never()).delete(comment);
    }

    @Test
    @DisplayName("Should hard-delete comment when no replies exist")
    void testHardDeleteCommentWithoutReplies() {
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setAuthor(author);
        comment.setTask(task);
        comment.setReplies(Collections.emptyList());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(authentication.getName()).thenReturn("author@example.com");
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

        commentService.deleteComment(commentId, authentication);

        verify(commentRepository).delete(comment);
    }
}
