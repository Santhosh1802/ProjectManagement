package com.Santhosh1802.ProjectManagement.exception.task;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
