package hu.econsult.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.econsult.jwt.JwtAuthRequest;
import hu.econsult.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserService userService;

	@Autowired
	public AuthController(UserService userService) {
		super();
		this.userService = userService;
	}

	// Token generating
	@PostMapping("/")
	public ResponseEntity<Object> auth(@RequestBody @Valid JwtAuthRequest request) {
		return userService.findByUsernameAndPassword(request);
	}

	
	@PutMapping("/logout")
	public ResponseEntity<?> logoutUser(@RequestHeader(name = "Authorization") String token) {
		return userService.logoutUser(token);
	}

}
