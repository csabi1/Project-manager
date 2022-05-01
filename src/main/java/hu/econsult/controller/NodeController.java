package hu.econsult.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import hu.econsult.model.dto.CreateChildDto;
import hu.econsult.model.dto.DetacheDto;
import hu.econsult.model.dto.ListDto;
import hu.econsult.model.dto.ModifyNodeDto;
import hu.econsult.model.dto.NodeCreateDto;
import hu.econsult.model.dto.RoleDto;
import hu.econsult.model.entity.Node;
import hu.econsult.service.NodeService;

@RestController
@RequestMapping("api/nodes")
public class NodeController {

	private final NodeService nodeService;

	@Autowired
	public NodeController(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@GetMapping("/get-own-project")
	public ResponseEntity<List<Node>> getOwnProjects(@RequestHeader(name = "Authorization") String token) {
		return nodeService.getOwnProject(token);
	}

	@GetMapping("/get-all-ongoing-project")
	public @ResponseBody ResponseEntity<?> getAllOngoingProject(@RequestHeader(name = "Authorization") String token) {
		return nodeService.getAllOngoingProject();
	}
	
	@PostMapping("/create-project")
	public ResponseEntity<?> createNode(@RequestHeader(name = "Authorization") String token, @RequestBody  NodeCreateDto nodeCreateDto) {
		return ResponseEntity.ok(nodeService.createProject(token, nodeCreateDto));
	}
	
	@PostMapping("/create-child")
	public ResponseEntity<?> createChild(@RequestHeader(name = "Authorization") String token,  @RequestBody  CreateChildDto createChildDto) {
		return ResponseEntity.ok(nodeService.createChild(token, createChildDto, createChildDto.getNodeId()));
	}
	
	@PutMapping("/detache-node-as-independent-poject") 
	public ResponseEntity<Node> detacheNodeAsIndependentProject(@RequestHeader(name = "Authorization") String token, @RequestBody DetacheDto detacheDto)	{ 
		return nodeService.detacheNodeAsIndependentProject(token , detacheDto);
	}
		
	@PostMapping("/set-users-to-node/{nodeId}")
	public ResponseEntity<?> setUsersToNode(@RequestHeader(name = "Authorization") String token, @PathVariable("nodeId") Long nodeId, @RequestBody ListDto listDto) { 
		return nodeService.assignUsersToNode(nodeId, listDto.getList());	
	}
	
	@PatchMapping("/set-project-active-inactive/nodeId/{nodeId}/isActive/{isActive}")
	public ResponseEntity<?> setProjectActiveInactive(@RequestHeader(name = "Authorization") String token, @PathVariable("isActive") Boolean isActive, @PathVariable("nodeId") Long nodeId) { 
		return nodeService.setProjectActiveInactive(isActive, nodeId);
	}
	
	@PutMapping("/merge-projects/rootProject/{rootProject}/nodeProject/{nodeProject}")
	public ResponseEntity<Node> mergeProjects(@RequestHeader(name = "Authorization") String token, @PathVariable("rootProject") Long rootProject, @PathVariable("nodeProject") Long nodeProject) {
		return nodeService.mergeProjects(rootProject, nodeProject);	
	}
	
	@PatchMapping("/modify-node")
	public ResponseEntity<Node> modifyNode(@RequestHeader(name = "Authorization") String token, @RequestBody ModifyNodeDto modifyNodeDto) {
		return nodeService.modifyNode(token, modifyNodeDto);
	}
	
	@PutMapping("/add-node-role-to-user/userId/{userId}/nodeId/{nodeId}") 
	public ResponseEntity<?> setUserRoleForNode(@RequestHeader(name = "Authorization") String token, @PathVariable("userId") Long userId, @PathVariable("nodeId") Long nodeId, @RequestBody RoleDto roleDto) {
		return nodeService.setUserRoleForNode(token, userId, nodeId, roleDto.getRoleCode());
	}
//	@PutMapping("/rename-project/{nodeId}")
//	public ResponseEntity<Node> renameProject(@RequestHeader(name = "Authorization") String token, @PathVariable("nodeId") Long nodeId, @RequestBody RenameDto renameDto) {
//		return nodeService.renameProject(nodeId, renameDto.getNewName());
//	}
	
	@DeleteMapping("/delete-node/{nodeId}")
	public ResponseEntity<Node> deleteNode(@RequestHeader(name = "Authorization") String token, @PathVariable("nodeId") Long nodeId) {
		return nodeService.deleteNodeTree(nodeId);
	}
	
	@GetMapping("/get-user-node-roles/user/{userId}/node/{nodeId}")
	public ResponseEntity<List<String>> getUserNodeRoles(@RequestHeader(name = "Authorization") String token,
			@PathVariable("userId") Long userId, @PathVariable("nodeId") Long nodeId){
		return nodeService.getUserNodeRoles(token, userId, nodeId);
	} 
		
}
