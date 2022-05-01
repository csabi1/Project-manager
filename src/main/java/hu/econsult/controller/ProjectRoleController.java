package hu.econsult.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.econsult.model.dto.ProjectRoleCreateDto;
import hu.econsult.model.entity.ProjectRole;
import hu.econsult.service.ProjectRoleService;

@RestController
@RequestMapping("/api")
public class ProjectRoleController {

	private final ProjectRoleService projectRoleService;

	public ProjectRoleController(ProjectRoleService projectRoleService) {
		this.projectRoleService = projectRoleService;
	}

	// project roles list
	@GetMapping("/get-project-roles")
	public ResponseEntity<List<ProjectRole>> getProjectRoles(@RequestHeader(name = "Authorization") String token) {
		return projectRoleService.findAll(token);
	}

	// create new project ROLE
	@PostMapping("/project-role/create")
	public ResponseEntity<ProjectRole> createProjectRole(@RequestHeader(name = "Authorization") String token,
			@RequestBody @Valid ProjectRoleCreateDto roleCreateDto) {
		return projectRoleService.createProjectRole(token, roleCreateDto.getRolename());
	}

	// delete project role by roleId
	@DeleteMapping("/role/delete-by-role-id/{projectRoleId}")
	public void deleteProjectRole(@RequestHeader(name = "Authorization") String token, @PathVariable("projectRoleId") Long projectRoleId) {
		projectRoleService.deleteById(token, projectRoleId);
	}
	
	//add project role for user
	@PatchMapping("/add-project-role/{projectRoleId}/user/{userId}")
	public ResponseEntity<?> addProjectRoleByUserId(
			@RequestHeader(name = "Authorization") String token, 
			@PathVariable("userId") Long userId, 
			@PathVariable("projectRoleId") Long projectRoleId){
		return projectRoleService.addProjectRoleByUserId(token, userId, projectRoleId);
	}
	

	@PatchMapping("/withdraw-project-role/{projectRoleId}/user/{userId}")
	public ResponseEntity<?> withdrawProjectRoleFromUser(
			@RequestHeader(name = "Authorization") String token, 
			@PathVariable("userId") Long userId, 
			@PathVariable("projectRoleId") Long projectRoleId){
		return projectRoleService.withdrawProjectRole(token, userId, projectRoleId);
	}
}
