package hu.econsult.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import hu.econsult.model.entity.User;
import hu.econsult.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.java.Log;

@Component
@Log
public class JwtProvider {
	
	@Autowired
	private  UserRepository userRepository;
	
	@Value("$(jwt.secret)")
	private String jwtSecret;
	
	
	public boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			log.severe("Invalid token");
		}
		return false;
	}
	
	public String getUsernameFromToken(String token) {
		token = token.replace("Bearer ", "");
		Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
		return claims.getSubject();
	}
	
	public Long getUserIdFromToken(String token) {
		token = token.replace("Bearer ", "");
		Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
		return Long.parseLong(claims.get("userId").toString());
	}

	/**
	 * Az aktuálisan bejelentkezett felhasználó visszatérítése a Authorization Header alapján.
	 * @param token
	 * @return
	 */
	public User getUserFromToken(String token) {
		String username = getUsernameFromToken(token);
		User user = userRepository.findByUsername(username).get();
		return user;
	}
}
