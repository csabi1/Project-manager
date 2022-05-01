package hu.econsult.model.dto;

import hu.econsult.model.entity.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChildNodeCreateDto {

	private String nodeDescription;
	private String nodeName;
	private Boolean isFileSupported;
	
	public Node convertToNode(String projectName) {
		Node node = new Node();
		node.setIsFileSupport(isFileSupported);
		node.setProjectName(projectName);
		node.setNodeDescription(nodeDescription);
		node.setIsActive(true);
		node.setNodeName(nodeName);
		return node;
	}
}

