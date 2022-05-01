package hu.econsult.service.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import hu.econsult.exceptions.CustomMessageException;
import hu.econsult.exceptions.UserRequestNotAllowedException;
import hu.econsult.jwt.ValidatorService;
import hu.econsult.model.ProjectRoles;
import hu.econsult.model.entity.ProjectRole;
import hu.econsult.model.entity.User;
import hu.econsult.repository.ProjectRoleRepository;
import hu.econsult.service.ProjectRoleService;
import hu.econsult.service.UserService;

@Service
public class ProjectRoleServiceImpl implements ProjectRoleService {
	
	private final UserService userService;
	private final ValidatorService validatorService;
	private final ProjectRoleRepository projectRoleRepository;
	
	@Autowired
	public ProjectRoleServiceImpl(ProjectRoleRepository projectRoleRepository,
			ValidatorService validatorService, UserService userService) {
		this.userService = userService;
		this.validatorService = validatorService;
		this.projectRoleRepository = projectRoleRepository;
	}
	

	@Override
	public ResponseEntity<List<ProjectRole>> findAll(String token) {
		if(!validatorService.isValidToken(token)) {
			throw new CustomMessageException("Érvénytelen token.", HttpStatus.BAD_REQUEST);
		}
		if(!userService.userHasProjectRole(token, ProjectRoles.ROLE_PROJECT_ADMIN)) {
			throw new UserRequestNotAllowedException();
		}
		List<ProjectRole> projectRoles = projectRoleRepository.findAll();
		if(projectRoles == null) throw new NoSuchElementException();
		return ResponseEntity.ok(projectRoles);
	}

	
	/**
	 * Új Project manager szerepkör létrehozás.
	 */
	@Override
	public ResponseEntity<ProjectRole> createProjectRole(String token, String rolename) {
		if(!validatorService.isValidToken(token)) {
			throw new CustomMessageException("Érvénytelen token.", HttpStatus.BAD_REQUEST);
		}
		if(!userService.userHasProjectRole(token, ProjectRoles.ROLE_PROJECT_ADMIN)) {
			throw new UserRequestNotAllowedException();
		}
		ProjectRole existProjectRole = projectRoleRepository.findByProjectRole(rolename);
		if(existProjectRole != null) {
			throw new CustomMessageException("Van már ilyen ROLE!", HttpStatus.BAD_REQUEST);
		}
		ProjectRole newProjectRole = new ProjectRole();
		newProjectRole.setProjectRole(rolename);
		projectRoleRepository.save(newProjectRole);
		return ResponseEntity.ok(newProjectRole);
	}

	/**
	 * Project manager szerepkör törlése.
	 */
	@Override
	public void deleteById(String token, Long projectRoleId) {
		if(!userService.userHasProjectRole(token, ProjectRoles.ROLE_PROJECT_ADMIN)) {
			throw new UserRequestNotAllowedException();
		}
		projectRoleRepository.deleteById(projectRoleId);
	}
	
	
	/**
	 * Project manager szerepkör adás felhasználónak userId és roleId alapján.
	 * Szerpkör adás más felhasználónak csak ADMIN jogosultsággal lehet.
	 */
	@Override
	public ResponseEntity<?> addProjectRoleByUserId(String token, Long userId, Long projectRoleId) {
		if(!userService.userHasProjectRole(token, ProjectRoles.ROLE_PROJECT_ADMIN)) {
			throw new UserRequestNotAllowedException();
		}
		ProjectRole newProjectRole = projectRoleRepository.findById(projectRoleId).orElse(null);
		if(newProjectRole == null) {
			throw new CustomMessageException("Nincs ilyen Project Role!", HttpStatus.BAD_REQUEST);
		}
		User user = userService.findById(userId);
		Set<ProjectRole> userCurrentProjectRoles = user.getProjectRoles();
		userCurrentProjectRoles.add(newProjectRole);
		userService.saveUser(user);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	/**
	 * Szerepkör visszavonás.
	 * @param userId
	 * @param projectRoleId
	 * @return
	 */
	@Override
	public ResponseEntity<?> withdrawProjectRole(String token, Long userId, Long projectRoleId) {
		if(!userService.userHasProjectRole(token, ProjectRoles.ROLE_PROJECT_ADMIN)) {
			throw new UserRequestNotAllowedException();
		}
		if(!projectRoleRepository.existsById(projectRoleId)) {
			throw new CustomMessageException("Nincs ilyen Project Role!", HttpStatus.BAD_REQUEST);
		}
		User user = userService.findById(userId);
		Set<ProjectRole> userCurrentProjectRoles = user.getProjectRoles();
		userCurrentProjectRoles.remove(projectRoleRepository.findById(projectRoleId).get());
		user.setProjectRoles(userCurrentProjectRoles);
		userService.saveUser(user);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
