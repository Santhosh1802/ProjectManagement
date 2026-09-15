package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.project.*;
import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.dto.response.project.ProjectOwnerResponse;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private GetProjectResponse returnProjectResponse(Project project){
        return new GetProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getProjectStatus(),
                project.getStartDate(),
                project.getDueDate(),
                new ProjectOwnerResponse(project.getOwner().getId(),
                        project.getOwner().getFirstName(),
                        project.getOwner().getLastName(),
                        project.getOwner().getEmail()),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    @Transactional(rollbackFor = {UserNotFoundException.class, ProjectAlreadyExistException.class})
    public GetProjectResponse createProject(CreateProjectRequest createProjectRequest) {
        boolean projectExist = projectRepository.findProjectByName(createProjectRequest.getName()).isPresent();
        if (projectExist) {
            throw new ProjectAlreadyExistException("Project with name " + createProjectRequest.getName() + " already exists");
        } else {
            User owner = userRepository.findById(createProjectRequest.getOwner()).orElseThrow(() -> new UserNotFoundException("User with id " + createProjectRequest.getOwner() + " not found"));

            Project project = new Project();
            project.setName(createProjectRequest.getName());
            project.setDescription(createProjectRequest.getDescription());
            project.setProjectStatus(ProjectStatus.PLANNING);
            project.setStartDate(createProjectRequest.getStartDate());
            project.setDueDate(createProjectRequest.getDueDate());
            project.setOwner(owner);
            Project savedProject = projectRepository.save(project);
            return returnProjectResponse(savedProject);
        }


    }

    public GetProjectResponse getProjectById(GetProjectByIdRequest getProjectByIdRequest) {
        Project project = projectRepository.findById(getProjectByIdRequest.getId()).orElseThrow(() ->
                new ProjectNotFoundException("Project id " + getProjectByIdRequest.getId() + " not found")
        );
        return returnProjectResponse(project);
    }

    public List<GetProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        List<GetProjectResponse> getProjectResponses = new ArrayList<>();
        for (Project project : projects) {
            getProjectResponses.add(returnProjectResponse(project));
        }
        return getProjectResponses;
    }

    public List<GetProjectResponse> getProjectsByOwner(GetProjectByOwnerIdRequest getProjectByOwnerIdRequest) {
        List<Project> projects = projectRepository.findAll();
        List<GetProjectResponse> projectResponses = new ArrayList<>();
        for (Project project : projects) {
            if(project.getOwner().getId().equals(getProjectByOwnerIdRequest.getOwnerId())) {
                projectResponses.add(returnProjectResponse(project));
            }
        }
        return projectResponses;
    }


    @Transactional(rollbackFor = {})
    public GetProjectResponse updateProjectDetails(UpdateProjectDetailsRequest updateProjectDetailsRequest) {
        Project project = projectRepository.findById(updateProjectDetailsRequest.getId()).orElseThrow(()->
                new ProjectNotFoundException("Project id " + updateProjectDetailsRequest.getId() + " not found")
        );
        if(!project.getName().equals(updateProjectDetailsRequest.getName())){
            boolean duplicateProjectName = projectRepository.findProjectByName(updateProjectDetailsRequest.getName()).isPresent();
            if(duplicateProjectName){
                throw new ProjectAlreadyExistException("Project with name " + updateProjectDetailsRequest.getName() + " already exists");
            }
            else{
                project.setName(updateProjectDetailsRequest.getName());
                project.setDescription(updateProjectDetailsRequest.getDescription());
                project.setProjectStatus(updateProjectDetailsRequest.getProjectStatus());
                project.setDueDate(updateProjectDetailsRequest.getDueDate());
                projectRepository.save(project);
                return returnProjectResponse(project);
            }

        }
        else{
            project.setName(updateProjectDetailsRequest.getName());
            project.setDescription(updateProjectDetailsRequest.getDescription());
            project.setProjectStatus(updateProjectDetailsRequest.getProjectStatus());
            project.setDueDate(updateProjectDetailsRequest.getDueDate());
            projectRepository.save(project);
            projectRepository.save(project);
            return returnProjectResponse(project);
        }

    }

    @Transactional(rollbackFor = {})
    public GetProjectResponse updateProjectOwner(UpdateProjectOwnerRequest updateProjectOwnerRequest) {
        Project project = projectRepository.findById(updateProjectOwnerRequest.getId()).orElseThrow(()->
                new ProjectNotFoundException("Project id " + updateProjectOwnerRequest.getId() + " not found")
        );
        User owner = userRepository.findById(updateProjectOwnerRequest.getOwnerId()).orElseThrow(()->
                new UserNotFoundException("User with id " + updateProjectOwnerRequest.getOwnerId() + " not found")
        );
        project.setOwner(owner);
        projectRepository.save(project);
        return returnProjectResponse(project);
    }

    @Transactional(rollbackFor = {})
    public boolean deleteProject(DeleteProjectRequest deleteProjectRequest) {
        projectRepository.findById(deleteProjectRequest.getId()).orElseThrow(()->
                new ProjectNotFoundException("Project id " + deleteProjectRequest.getId() + " not found")
        );

        projectRepository.deleteById(deleteProjectRequest.getId());
        return true;
    }
}
