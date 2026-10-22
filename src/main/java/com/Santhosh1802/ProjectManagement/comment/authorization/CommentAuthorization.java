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
}
