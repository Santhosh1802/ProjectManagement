package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.projectMember.CreateProjectMemberRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.DeleteProjectMemberRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.GetByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.UpdateProjectMemberRoleRequest;
import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.dto.response.project.ProjectOwnerResponse;
import com.Santhosh1802.ProjectManagement.dto.response.projectMember.GetProjectMemberResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.ProjectMember;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.projectMember.ProjectMemberAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.projectMember.ProjectMemberNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectMemberService {

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private GetProjectMemberResponse returnGetProjectMemberResponse(ProjectMember projectMember) {
        return new GetProjectMemberResponse(
                projectMember.getId(),
                new GetUserResponse(
                        projectMember.getUser().getId(),
                        projectMember.getUser().getFirstName(),
                        projectMember.getUser().getLastName(),
                        projectMember.getUser().getEmail(),
                        projectMember.getUser().getUserRole(),
                        projectMember.getUser().getIsActive(),
                        projectMember.getUser().getEmailVerified(),
                        projectMember.getUser().getCreatedAt(),
                        projectMember.getUser().getUpdatedAt(),
                        projectMember.getUser().getLastLoginAt()
                ),
                new GetProjectResponse(
                        projectMember.getProject().getId(),
                        projectMember.getProject().getName(),
                        projectMember.getProject().getDescription(),
                        projectMember.getProject().getProjectStatus(),
                        projectMember.getProject().getStartDate(),
                        projectMember.getProject().getDueDate(),
                        new ProjectOwnerResponse(projectMember.getProject().getOwner().getId(),
                                projectMember.getProject().getOwner().getFirstName(),
                                projectMember.getProject().getOwner().getLastName(),
                                projectMember.getProject().getOwner().getEmail()),
                        projectMember.getProject().getCreatedAt(),
                        projectMember.getProject().getUpdatedAt()
                ),
                projectMember.getProjectRole(),
                projectMember.getJoinedAt()
        );
    }

    @Transactional(rollbackFor = {UserNotFoundException.class,ProjectNotFoundException.class, ProjectMemberAlreadyExistException.class})
    public GetProjectMemberResponse addMember(CreateProjectMemberRequest createProjectMemberRequest) {
        ProjectMember projectMember = new ProjectMember();
        User user = userRepository.findById(createProjectMemberRequest.getProjectMemberId()).orElseThrow(() ->
                new UserNotFoundException("User of id: " + createProjectMemberRequest.getProjectMemberId() + " not found.")
        );
        Project project = projectRepository.findById(createProjectMemberRequest.getProjectId()).orElseThrow(() ->
                new ProjectNotFoundException("Project of id: " + createProjectMemberRequest.getProjectId() + " not found")
        );
        projectMember.setUser(user);
        projectMember.setProject(project);
        boolean memberExist = projectMemberRepository.existsByUserIdAndProjectId(user.getId(),project.getId());
        if(memberExist){
            throw new ProjectMemberAlreadyExistException("Project Member of user: " + user.getId() + " already exist.");
        }
        projectMember.setProjectRole(createProjectMemberRequest.getProjectRole());
        projectMemberRepository.save(projectMember);
        return returnGetProjectMemberResponse(projectMember);
    }

    public GetProjectMemberResponse getMemberById(GetByIdRequest getByIdRequest) {
        ProjectMember projectMember = projectMemberRepository.findById(getByIdRequest.getId()).orElseThrow(()->new ProjectMemberNotFoundException("Project Member with id: " + getByIdRequest.getId() + " not found."));
        return returnGetProjectMemberResponse(projectMember);
    }

    public List<GetProjectMemberResponse> getMembersByProject(GetByIdRequest getByIdRequest) {
        Project project = projectRepository.findById(getByIdRequest.getId()).orElseThrow(() ->
                new ProjectNotFoundException("Project of id: " + getByIdRequest.getId() + " not found.")
        );
        List<GetProjectMemberResponse> projectMembers = new ArrayList<>();
        project.getMembers().forEach((projectMember ->
                projectMembers.add(returnGetProjectMemberResponse(projectMember))
        ));
        return projectMembers;
    }

    @Transactional(rollbackFor = {UserNotFoundException.class})
    public List<GetProjectMemberResponse> getProjectsByUser(GetByIdRequest getByIdRequest) {
        User user = userRepository.findById(getByIdRequest.getId()).orElseThrow(() ->
                new UserNotFoundException("User of id: " + getByIdRequest.getId() + " not found.")
        );
        List<GetProjectMemberResponse> projectMembers = new ArrayList<>();
        user.getProjectMemberships().forEach(projectMember ->
                projectMembers.add(returnGetProjectMemberResponse(projectMember)
                ));
        return projectMembers;

    }

    @Transactional(rollbackFor = {ProjectMemberNotFoundException.class})
    public GetProjectMemberResponse updateMemberRole(UpdateProjectMemberRoleRequest updateProjectMemberRoleRequest) {
        ProjectMember projectMember = projectMemberRepository.findById(updateProjectMemberRoleRequest.getId()).orElseThrow(() -> new ProjectMemberNotFoundException("Project member with id: " + updateProjectMemberRoleRequest.getId() + " not found."));
        projectMember.setProjectRole(updateProjectMemberRoleRequest.getProjectRole());
        projectMemberRepository.save(projectMember);
        return returnGetProjectMemberResponse(projectMember);
    }

    @Transactional(rollbackFor = {ProjectMemberNotFoundException.class})
    public boolean removeMember(DeleteProjectMemberRequest deleteProjectMemberRequest) {
        projectMemberRepository.delete(projectMemberRepository.findById(deleteProjectMemberRequest.getId()).orElseThrow(() -> new ProjectMemberNotFoundException("Project Member of id: " + deleteProjectMemberRequest.getId() + " not found.")));
        return true;
    }
}
