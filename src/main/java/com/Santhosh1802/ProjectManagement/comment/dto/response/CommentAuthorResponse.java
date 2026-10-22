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
public class CommentAuthorResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}
