package hu.econsult.model.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class RenameDto {
	@NotNull
	private String newName;
}
