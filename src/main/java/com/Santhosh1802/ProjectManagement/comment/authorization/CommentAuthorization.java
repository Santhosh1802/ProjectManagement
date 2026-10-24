package com.Santhosh1802.ProjectManagement.comment.authorization;

import com.Santhosh1802.ProjectManagement.authorization.TaskAuthorization;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("commentAuthorization")
public class CommentAuthorization {

    private final TaskAuthorization taskAuthorization;

    public CommentAuthorization(TaskAuthorization taskAuthorization) {
        this.taskAuthorization = taskAuthorization;
    }

    /**
     * Users can comment on a task if they have permission to view the task
     * (project owner, project member, or assignee).
     */
    public boolean canCommentOnTask(UUID taskId, Authentication authentication) {
        return taskAuthorization.canViewTask(taskId, authentication);
    }

    /**
     * Users can edit a comment only if they are the author.
     */
    public boolean canEditComment(com.Santhosh1802.ProjectManagement.comment.entity.Comment comment, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || comment == null) {
            return false;
        }
        return comment.getAuthor() != null && comment.getAuthor().getEmail().equals(authentication.getName());
    }

    /**
     * Users can delete a comment if they are the author, an ADMIN, or a project manager/owner for the task.
     */
    public boolean canDeleteComment(com.Santhosh1802.ProjectManagement.comment.entity.Comment comment, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || comment == null) {
            return false;
        }
        String email = authentication.getName();
        if (comment.getAuthor() != null && comment.getAuthor().getEmail().equals(email)) {
            return true;
        }
        if (comment.getTask() != null) {
            return taskAuthorization.canManageTask(comment.getTask().getId(), authentication);
        }
        return false;
    }
}
