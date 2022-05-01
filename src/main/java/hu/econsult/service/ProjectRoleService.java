package hu.econsult.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import hu.econsult.model.entity.ProjectRole;

public interface ProjectRoleService {

	ResponseEntity<List<ProjectRole>> findAll(String token);

	ResponseEntity<ProjectRole> createProjectRole(String token, String rolename);

	void deleteById(String token, Long projectRoleId);
	
	ResponseEntity<?> addProjectRoleByUserId(String token, Long userId, Long projectRoleId);
	
	ResponseEntity<?> withdrawProjectRole(String token, Long userId, Long projectRoleId);

}
