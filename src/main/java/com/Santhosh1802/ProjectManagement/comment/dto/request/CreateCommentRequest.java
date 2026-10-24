package com.Santhosh1802.ProjectManagement.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

    @NotNull(message = "Task ID cannot be null")
    private UUID taskId;

    private UUID parentCommentId;

    @NotBlank(message = "Comment content cannot be blank")
    @Size(max = 4000, message = "Comment cannot exceed 4000 characters")
    private String content;

    public CreateCommentRequest(UUID taskId, String content) {
        this.taskId = taskId;
        this.content = content;
    }
}
