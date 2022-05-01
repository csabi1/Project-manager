package hu.econsult.service;

import org.springframework.http.ResponseEntity;

import hu.econsult.jwt.JwtAuthRequest;
import hu.econsult.model.entity.User;

public interface UserService {
	
	User findById(Long userId);

	User findByUsername(String username);
	
	public void saveUser(User user);

	ResponseEntity<Object> findByUsernameAndPassword(JwtAuthRequest request);

	public ResponseEntity<?> logoutUser(String token);
	
	boolean userHasRole(Long userId, String roleCode, Long nodeId);
	
	boolean userHasProjectRole(String token, String projectRoleCode);
}
