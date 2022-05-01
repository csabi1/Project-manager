package hu.econsult.jwt;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthRequest {

	@NotNull(message = "Felhasználónevet meg kell adni!")
	private String username;
	
	@NotNull(message = "Jelszót meg kell adni!")
	private String password;
}
