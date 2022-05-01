package hu.econsult.model.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRoleCreateDto {

	@NotNull(message = "Role nevét meg kell adni!")
	@Pattern(regexp = "^[ROLE_]+[A-Z_]*", message="A ROLE formátuma nem mefelelő!")
	private String rolename;
}
