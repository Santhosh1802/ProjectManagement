package com.Santhosh1802.ProjectManagement.comment.controller;

import com.Santhosh1802.ProjectManagement.comment.dto.request.CreateCommentRequest;
import com.Santhosh1802.ProjectManagement.comment.dto.response.GetCommentResponse;
import com.Santhosh1802.ProjectManagement.comment.service.CommentService;
import com.Santhosh1802.ProjectManagement.util.ApiResponse;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@Validated
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Add a comment to a task.
     * POST /api/comments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GetCommentResponse>> addComment(
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication
    ) {
        GetCommentResponse commentResponse = commentService.addComment(request, authentication);

        ApiResponse<GetCommentResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.CREATED.value(),
                "Comment created successfully",
                commentResponse
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get paginated comments for a task.
     * GET /api/comments/task/{taskId}
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<PageResponse<GetCommentResponse>>> getCommentsByTask(
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page cannot be negative") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1") @Max(value = 100, message = "Size cannot exceed 100") int size,
            Authentication authentication
    ) {
        PageResponse<GetCommentResponse> comments = commentService.getCommentsByTask(taskId, page, size, authentication);

        ApiResponse<PageResponse<GetCommentResponse>> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Comments retrieved successfully",
                comments
        );

        return ResponseEntity.ok(response);
    }
}
