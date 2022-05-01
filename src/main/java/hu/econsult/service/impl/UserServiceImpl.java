package hu.econsult.service.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import hu.econsult.jwt.ValidatorService;
import hu.econsult.jwt.JwtAuthRequest;
import hu.econsult.jwt.JwtProvider;
import hu.econsult.model.entity.ProjectRole;
import hu.econsult.model.entity.User;
import hu.econsult.repository.UserRepository;
import hu.econsult.service.UserService;

@Service
public class UserServiceImpl implements UserService {
	
	private final JwtProvider jwtProvider;
	private final UserRepository userRepository;
	private final ValidatorService validatorService;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepository, JwtProvider jwtProvider,
			ValidatorService validatorService) {
		this.jwtProvider = jwtProvider;
		this.userRepository = userRepository;
		this.validatorService = validatorService;
	}

	@Override
	public User findById(Long userId) {
		User u = userRepository.findById(userId).get();
		if(u == null) throw new NoSuchElementException();
		return u;
	}

	@Override
	public User findByUsername(String username) {
		return userRepository.findByUsername(username).get();
	}
	
	
	@Override
	public ResponseEntity<Object> findByUsernameAndPassword(JwtAuthRequest request) {
		JsonNode token =  validatorService.authenticate(request.getUsername(), request.getPassword());
		return new ResponseEntity<>(token, HttpStatus.ACCEPTED);
	}
	
	
	@Override
	public boolean userHasRole(Long userId, String roleCode, Long nodeId) {
		return userRepository.userHasGivenRole(userId, nodeId, roleCode); 
	}

	
	@Override
	public void saveUser(User user) {
		userRepository.save(user);
	}
	
	
	@Override
	public ResponseEntity<?> logoutUser(String token) {
		validatorService.logoutToken(token);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	
	/**
	 * True ha a felhasználó rendelkezik a megadott Project Role -lal.
	 */
	@Override
	public boolean userHasProjectRole(String token, String projectRoleCode) {
		User user = jwtProvider.getUserFromToken(token);
		List<ProjectRole> userProjectRoles = user.getProjectRoles().stream()
				.filter(u -> u.getProjectRole().equals(projectRoleCode))
				.collect(Collectors.toList());
		return userProjectRoles.size() > 0;
	}

}
