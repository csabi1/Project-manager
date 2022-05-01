package hu.econsult.model.dto;

import hu.econsult.model.entity.Node;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NodeCreateDto {

	private String nodeDescription;
	private String projectName;
	private Boolean isFileSupported;
	
	public Node convertToNode() {
		Node node = new Node();
		node.setIsFileSupport(isFileSupported);
		node.setProjectName(projectName);
		node.setNodeDescription(nodeDescription);
		node.setIsActive(true);
		node.setNodeName(projectName);
		return node;
	}
}
