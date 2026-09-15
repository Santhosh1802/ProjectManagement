package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.entity.ProjectMember;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = {})
public class ProjectMemberService {

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    public ProjectMember addMember(ProjectMember member){
        return null;
    }

    public ProjectMember getMemberById(UUID id){
        return null;
    }

    public List<ProjectMember> getMembersByProject(UUID projectId){
        return null;
    }

    public List<ProjectMember> getProjectsByUser(UUID userId){
        return null;
    }

    public ProjectMember updateMemberRole(UUID id, ProjectRole role){
        return null;
    }

    public void removeMember(UUID id){

    }
}
