package hu.econsult.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import hu.econsult.model.dto.CreateChildDto;
import hu.econsult.model.dto.DetacheDto;
import hu.econsult.model.dto.ModifyNodeDto;
import hu.econsult.model.dto.NodeCreateDto;
import hu.econsult.model.entity.Node;

public interface NodeService {
	
    Node createProject(String token, NodeCreateDto nodeCreateDto);

	Node getNode(Long projectId);

	ResponseEntity<Node> deleteNodeTree(Long nodeId);

	ResponseEntity<Node> modifyNode(String token, ModifyNodeDto modifyNodeDto);

	ResponseEntity<?> getAllOngoingProject();

	Node createChild(String token, CreateChildDto nodeCreateDto, Long parentId);

	ResponseEntity<?> setProjectActiveInactive(Boolean isActive, Long nodeId);

	ResponseEntity<Node> mergeProjects(Long rootProject, Long nodeProject);

	ResponseEntity<Node> renameProject(Long nodeId, String newName);

	ResponseEntity<?> assignUsersToNode(Long nodeId, List<Long> list);

	ResponseEntity<List<Node>> getOwnProject(String token);

	ResponseEntity<?> setUserRoleForNode(String token, Long userId, Long nodeId, String roleCode);

	ResponseEntity<Node> detacheNodeAsIndependentProject(String token, DetacheDto detacheDto);

	ResponseEntity<List<String>> getUserNodeRoles(String token, Long userId, Long nodeId);
}
