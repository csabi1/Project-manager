package hu.econsult.model.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ModifyNodeDto {
	
	@NotNull
	private Long nodeId; 
	
	private Boolean isActive;
	
	private Boolean isFileSupport;
	
	private String nodeDescription;
	
	private String nodeName;
	
	private String projectName;

}
