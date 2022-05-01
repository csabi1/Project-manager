package hu.econsult.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChildDto {
	private Long nodeId;
	private ChildNodeCreateDto childNodeCreateDto;
}
