package hu.econsult.model.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ListDto {
	@NotNull
	private List<Long> list;
}
