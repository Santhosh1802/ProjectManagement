package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.service.ProjectMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjectMemberController {

    @Autowired
    private ProjectMemberService projectMemberService;
}
