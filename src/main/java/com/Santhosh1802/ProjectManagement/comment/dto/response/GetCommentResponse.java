package com.Santhosh1802.ProjectManagement.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetCommentResponse {
    private UUID id;
    private UUID taskId;
    private String content;
    private CommentAuthorResponse author;
    private Instant createdAt;
    private Instant updatedAt;
}
