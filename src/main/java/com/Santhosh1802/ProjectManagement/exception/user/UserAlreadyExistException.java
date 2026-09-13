package com.Santhosh1802.ProjectManagement.exception.user;

public class UserAlreadyExistException extends RuntimeException{

    public UserAlreadyExistException(String message){
        super(message);
    }
}
