package hu.econsult.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import hu.econsult.exceptions.CustomMessageException;
import hu.econsult.exceptions.UserRequestNotAllowedException;
import hu.econsult.jwt.JwtProvider;
import hu.econsult.model.ProjectRoles;
import hu.econsult.model.UserNodeRoles;
import hu.econsult.model.dto.CreateChildDto;
import hu.econsult.model.dto.DetacheDto;
import hu.econsult.model.dto.ModifyNodeDto;
import hu.econsult.model.dto.NodeCreateDto;
import hu.econsult.model.entity.Node;
import hu.econsult.model.entity.User;
import hu.econsult.repository.NodeRepository;
import hu.econsult.repository.NodeRolesRepository;
import hu.econsult.service.FileStorageService;
import hu.econsult.service.NodeService;
import hu.econsult.service.UserService;

@Service
public class NodeServiceImpl implements NodeService {

	private final NodeRepository nodeRepository;
	private final UserService userService;
	private final FileStorageService fileStorageService;
	private final NodeRolesRepository nodeRolesRepository;
	private final JwtProvider jwtProvider;

	@Autowired
	public NodeServiceImpl(NodeRepository nodeRepository, JwtProvider jwtProvider, UserService userService,
			FileStorageService fileStorageService, NodeRolesRepository nodeRolesRepository) {
		this.nodeRepository = nodeRepository;
		this.jwtProvider = jwtProvider;
		this.userService = userService;
		this.fileStorageService = fileStorageService;
		this.nodeRolesRepository = nodeRolesRepository;
	}

	/**
	 * Project (szülő nélküli) elem létrehozása.
	 * A létrehozó automatikusan ADMIN_ROLE jogok kap az adott csomóponthoz.
	 * @param token a felhasználó azonosítására szolgál
	 * @param nodeCreateDto a projekt csomópont adatit tartalmazza.
	 */
	@Secured(ProjectRoles.ROLE_PROJECT_ADMIN)
	@Override
	public Node createProject(String token, NodeCreateDto nodeCreateDto) {
		Node nodeToSave = nodeCreateDto.convertToNode();
		nodeToSave.getUsers().add(jwtProvider.getUserFromToken(token));
		Node savedNode = nodeRepository.save(nodeToSave);
		addAllRolesUpUntilHierarchyMatch(jwtProvider.getUserIdFromToken(token), UserNodeRoles.ADMIN_ROLE, savedNode.getId());
		return nodeToSave;
	}

	/**
	 * Gyerek node létrehozására szolgaló metódus.
	 * @param nodeCreateDto létrehozni kivánt csomópont adatai.
	 * @param parentId szülő azonosítója.
	 */
	@Override
	public Node createChild(String token ,CreateChildDto nodeCreateDto, Long parentId) {
		Node currentNode = nodeRepository.findById(parentId).get();

		if (!userHasRoleInHierarchy(jwtProvider.getUserIdFromToken(token), UserNodeRoles.ADMIN_ROLE, parentId)) {
			throw new CustomMessageException("Csak admin jogosultásággal hozhatsz létre új ágat!", HttpStatus.FORBIDDEN);
		}
		
		Node childNode = nodeCreateDto.getChildNodeCreateDto().convertToNode(currentNode.getProjectName());
		nodeRepository.save(childNode);
		Set<Node> nodeSet = currentNode.getChildren();
		nodeSet.add(childNode);
		currentNode.setChildren(nodeSet);
		nodeRepository.save(currentNode);
		return currentNode;
	}

	private boolean userHasRoleInHierarchy(Long userId, String roleCode, Long nodeId) {

		if (userService.userHasRole(userId, roleCode, nodeId)) {
			return true;
		}
		
		if (!nodeRepository.isChildNode(nodeId)) {
			return false;
		}
	
		return userHasRoleInHierarchy(userId, roleCode, nodeRepository.getParentNode(nodeId).getId());
	
	}

	@Override
	public Node getNode(Long projectId) {
		return nodeRepository.findById(projectId).orElse(null);
	}

	/**
	 * A csomópont törlésére szolgáló metódus. 
	 * Egy node törlése maga után vonja a teljes részfa törlését.
	 * @param nodeId törölni kivánt csomópont azonosítója.
	 */
	@Override
	public ResponseEntity<Node> deleteNodeTree(Long nodeId) {
		Node currentNode = getNode(nodeId);
		deleteFilesOfTree(currentNode);
		revokeAllRolesOfTree(currentNode);
		nodeRepository.delete(currentNode);
		return new ResponseEntity<Node>(HttpStatus.OK);
	}


	private void revokeAllRolesOfTree(Node node) {
		nodeRepository.deleteAllRoleByNode(node.getId());
		if (!isLeaf(node)) {
			node.getChildren().forEach(x -> revokeAllRolesOfTree(x));
		}
		
	}

	private void deleteFilesOfTree(Node node) {
		fileStorageService.deleteFilesOfNodes(node.getId());
		if (!isLeaf(node)) {
			node.getChildren().forEach(x -> deleteFilesOfTree(x));
		}
	}

	/**
	 * Csomópont leválasztása a fáról, mint gyökérelem.
	 * A leválasztás magával vonja a csomóponthoz tartozó összes eleme leválasztását.
	 * A methódus feltételezi, hogy a fa nem tartalmaz köröket.
	 * Az új gyökérelemet új névvel kell ellátni.
	 * @param token felhasználó azanosítása
	 * @param detacheDto leválasztani kivánt noode adatai.
	 * @return leválasztott részfa.
	 */
	@Override
	public ResponseEntity<Node> detacheNodeAsIndependentProject(String token, DetacheDto detacheDto) {
		Node nodeRoot = getNodeRoot(detacheDto.getNodeId());
		if (!userService.userHasRole(jwtProvider.getUserIdFromToken(token), UserNodeRoles.ADMIN_ROLE, nodeRoot.getId())) { 
			throw new CustomMessageException("Only project root admins are allowed to detache node as independent project!", HttpStatus.FORBIDDEN);
		}
		
		nodeRepository.deleteParentChildRelation(detacheDto.getNodeId());
		Node currentNode = nodeRepository.findById(detacheDto.getNodeId()).get();
		renameAllDescendant(currentNode, detacheDto.getNewName());
		return new ResponseEntity<>(currentNode, HttpStatus.OK);
	}

	private Node getNodeRoot(Long nodeId) {
		if (!nodeRepository.isChildNode(nodeId)) {
			return nodeRepository.findById(nodeId).get();
		} 
		return getNodeRoot(nodeRepository.getParentNode(nodeId).getId());
	}

	private void renameAllDescendant(Node currentNode, String newName) {
		currentNode.setProjectName(newName);
		if (!isLeaf(currentNode)) {
			currentNode.getChildren().forEach(x -> {
				renameAllDescendant(x, newName);
			});
		}
		nodeRepository.save(currentNode);

	}

	/**
	 * Visszadja az összes aktív státusszal rendelekző projektet.
	 */
	@Secured(ProjectRoles.ROLE_PROJECT_ADMIN)
	@Override
	public ResponseEntity<List<Node>> getAllOngoingProject() {
		List<Long> ongoingProjectIds = nodeRepository.getAllProjectId();
		List<Node> allOngoingProjects = nodeRepository.findAllById(ongoingProjectIds);
		return new ResponseEntity<>(allOngoingProjects.stream().filter(Node::getIsActive).collect(Collectors.toList()),
				HttpStatus.OK);
	}

	/**
	 * Project aktivását állító csomópont.
	 */
	@Override
	public ResponseEntity<?> setProjectActiveInactive(Boolean isActive, Long nodeId) {
		Node currentNode = getNode(nodeId);
		if (nodeRepository.isChildNode(nodeId)) {
			throw new CustomMessageException("Only project nodes activity can be modified", HttpStatus.BAD_REQUEST);
		}

		setActivityOfTree(currentNode, isActive);
		return ResponseEntity.ok(200);
	}

	private void setActivityOfTree(Node currentNode, Boolean isActive) {
		currentNode.setIsActive(isActive);
		if (!isLeaf(currentNode)) {
			currentNode.getChildren().forEach(x -> {
				setActivityOfTree(x, isActive);
			});
		}
		nodeRepository.save(currentNode);
	}

	private boolean isLeaf(Node currentNode) {
		return (currentNode.getChildren().isEmpty() || currentNode.getChildren() == null);
	}

	/**
	 * Két projekt egyesítésére szolgáló csomópont.
	 * Két root csomópont egyenrandú egyesítésére nem alkalmas.
	 * @param nodeProject annak a csomópontnak az azonosítoja amelyket a másik csomópont alá kívánunk vonni.
	 * @param rootProject 
	 */
	@Override
	public ResponseEntity<Node> mergeProjects(Long rootProject, Long nodeProject) {
		Node currentRootNode = nodeRepository.findById(rootProject).get();
		Node currentNodeProject = nodeRepository.findById(nodeProject).get();

		checkIfRootContainsCurrentNode(currentNodeProject, currentRootNode);

		if (!nodeRepository.isChildNode(nodeProject)) {
			addChildrenToParent(currentRootNode, currentNodeProject);
		} else {
			Node oldAncesterOfNode = nodeRepository.getParentNode(nodeProject);
			checkIfAlreadyParent(currentRootNode, oldAncesterOfNode);
			oldAncesterOfNode.getChildren().removeIf(x -> x.getId().equals(currentNodeProject.getId()));
			nodeRepository.save(oldAncesterOfNode);
			addChildrenToParent(currentRootNode, currentNodeProject);
		}

		nodeRepository.save(currentRootNode);
		return new ResponseEntity<>(currentRootNode, HttpStatus.OK);
	}

	private Integer checkIfRootContainsCurrentNode(Node currentRootNode, Node currentNodeProject) {
		if (currentRootNode.getId().equals(currentNodeProject.getId())) {
			throw new CustomMessageException("LOOP IN GRAPH: Cannot relocate node to its own child.",
					HttpStatus.BAD_REQUEST);
		}
		if (!isLeaf(currentRootNode)) {
			currentRootNode.getChildren().forEach(x -> checkIfRootContainsCurrentNode(x, currentNodeProject));
		}
		return 0;

	}

	private void checkIfAlreadyParent(Node currentRootNode, Node oldAncesterOfNode) {
		if (oldAncesterOfNode.getId().equals(currentRootNode.getId())) {
			throw new CustomMessageException("Trying to move node to its original parent.", HttpStatus.BAD_REQUEST);
		}
	}

	private void addChildrenToParent(Node currentRootNode, Node currentNodeProject) {
		renameAllDescendant(currentNodeProject, currentRootNode.getProjectName());
		currentRootNode.getChildren().add(currentNodeProject);
	}

	/**
	 * Project átnevezésére szolgáló metódus. 
	 * Az átnevezéssel a project alá tartózó össze csomópont projectName mezője beállításra kerül.
	 */
	@Override
	public ResponseEntity<Node> renameProject(Long nodeId, String newName) {
		Node currentNode = nodeRepository.findById(nodeId).get();
		if (nodeRepository.isChildNode(nodeId)) {
			currentNode.setNodeName(newName);
			nodeRepository.save(currentNode);
			return ResponseEntity.ok(currentNode);
		}

		renameAllDescendant(currentNode, newName);
		currentNode.setNodeName(newName);
		return ResponseEntity.ok(currentNode);
	}

	/**
	 * Felhasználó hozzárendelése egy adot node-hoz.
	 * @param nodeId node azonosítója.
	 * @
	 */
	@Override
	public ResponseEntity<?> assignUsersToNode(Long nodeId, List<Long> list) {
		Node currentNode = nodeRepository.getById(nodeId);
		List<Long> currentUsersAssignedToNode = currentNode.getUsers().stream().map(User::getId)
				.collect(Collectors.toList());

		if (listEqualsIgnoreOrder(list, currentUsersAssignedToNode)) {
			return ResponseEntity.ok(200);
		}

		List<Long> currentlySetUsers = currentNode.getUsers().stream().map(User::getId).collect(Collectors.toList());
		
		currentlySetUsers.forEach(x-> {
			if (!list.contains(x)) { 
				currentNode.getUsers().removeIf(y-> y.getId().equals(x));
				revokeRolesOfUserByNode(x, nodeId);
			}
		});
		
		list.forEach(x-> { 
			if (!currentlySetUsers.contains(x))  {
				assignUserToNode(currentNode, x);
			}
		});
		nodeRepository.save(currentNode);
		
		return ResponseEntity.ok(200);
	}

	private void assignUserToNode(Node currentNode, Long userId) {
		User currentUser = userService.findById(userId);
		currentNode.getUsers().add(currentUser);
		grantHighestRoleInUpperTree(userId, currentNode);
		
	}

	private void grantHighestRoleInUpperTree(Long userId, Node node ) {
		grantRoleToUser(userId, getHighestRoleInUpperTree(userId, node.getId()), node);
	}

	private String getHighestRoleInUpperTree(Long userId, Long nodeId) {
		String highestRole = UserNodeRoles.READ_ROLE;
		Node currentNode = nodeRepository.findById(nodeId).get();


		while (true) {
			String role = getHighestRoleOfNode(currentNode.getId(), userId);
			if (isHeigherInHierarchy(role, highestRole)) {
				highestRole = role;
			}
			
			if (!nodeRepository.isChildNode(currentNode.getId())) {
				break;
			}
			
			currentNode = getParent(currentNode.getId());
		}
		
		return highestRole;
	}

	private boolean isHeigherInHierarchy(String role, String highestRole) {
		return (UserNodeRoles.ROLE_HIERARCHY.indexOf(role) > UserNodeRoles.ROLE_HIERARCHY.indexOf(highestRole));
	}

	private Node getParent(Long id) {
		return nodeRepository.getParentNode(id);
	}

	private String getHighestRoleOfNode(Long nodeId, Long userId) {
		String max = UserNodeRoles.READ_ROLE;
		List<String> currentUserNodeRoles = nodeRepository.getAllNodeRoles(userId, nodeId); 
		for (String role : UserNodeRoles.ROLE_HIERARCHY) {
			if (currentUserNodeRoles.contains(role)) {
				max = role;
			}
		}
		return max;
	}

	private void revokeRolesOfUserByNode(Long userId, Long nodeId) {
		nodeRepository.deleteAllRoleByUserByNode(userId, nodeId);	
	}

	private <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
		return new HashSet<>(list1).equals(new HashSet<>(list2));
	}


	/**
	 * A felhasználóhoz tartozó projektetek lekérdezése
	 * @param token felhanszáló azonosítása.
	 */
	@Override
	public ResponseEntity<List<Node>> getOwnProject(String token) {
		Long userId = jwtProvider.getUserIdFromToken(token);
		return ResponseEntity.ok(getUsersProjectsById(userId));
	}

	private List<Node> getUsersProjectsById(Long userId) {
		return nodeRepository.getUsersProjectById(userId);
	}

	/**
	 * Node változtatására szolgáló metodódus.
	 * A végrehajtáshoz szükséges, hogy a bejelentkezett felahsználó rendelkezzen
	 * MODIFY_ROLE jogkörrel.
	 * Amennyiben a projekt neve nem gyökérelemben változik, a változtatás nem engedélyezett.
	 * Az aktív státuszt csak ADMIN_ROLE jogkörrel módosítható.
	 * @param token a felhasználó azonosítására szolgál
	 * @modifyNodeDto a módosítani kivánt adatok. 
	 */
	@Override
	public ResponseEntity<Node> modifyNode(String token, ModifyNodeDto modifyNodeDto) {
		Long userId = jwtProvider.getUserIdFromToken(token);
		if (!userService.userHasRole(userId, UserNodeRoles.MODIFY_ROLE, modifyNodeDto.getNodeId())) {
			throw new CustomMessageException("ACCESS DENIED. Modifying role required!  ", HttpStatus.FORBIDDEN);
		}

		Node currentNode = nodeRepository.findById(modifyNodeDto.getNodeId()).get();

		currentNode.setIsFileSupport(modifyNodeDto.getIsFileSupport());
		currentNode.setNodeDescription(modifyNodeDto.getNodeDescription());

		if (nodeRepository.isChildNode(currentNode.getId())) {
			currentNode.setNodeName(modifyNodeDto.getNodeName());

			if (!currentNode.getProjectName().equals(modifyNodeDto.getProjectName())) {
				throw new CustomMessageException("Projekt neve csak a projekt gyökérelemen változtatható meg",
						HttpStatus.BAD_REQUEST);
			}

		} else {
			if (!currentNode.getNodeName().equals(modifyNodeDto.getNodeName())) {
				throw new CustomMessageException("Projekt neve csak a projek név mezőn keresztül módosítható. ",
						HttpStatus.BAD_REQUEST);
			}
		}

		if (!currentNode.getProjectName().equals(modifyNodeDto.getProjectName())) {
			renameAllDescendant(currentNode, modifyNodeDto.getProjectName());
		}

		if (!currentNode.getIsActive().equals(modifyNodeDto.getIsActive())) {
			if (!userService.userHasRole(userId, UserNodeRoles.ADMIN_ROLE, modifyNodeDto.getNodeId())) {
				throw new CustomMessageException("ACCESS DENIED. Admin role requried to modify node active status!",
						HttpStatus.FORBIDDEN);
			}
			setActivityOfTree(currentNode, modifyNodeDto.getIsActive());
		}

		currentNode = nodeRepository.save(currentNode);
		return new ResponseEntity<>(currentNode, HttpStatus.OK);
	}

	/**
	 * Jogok beállítasa adott csomóponthoz.
	 * Csak ADMIN_ROLE jogkörrel rendlekező felhasználó használhatja.
	 * @param token felhasználó azonosítására szolgál
	 * @param userId jogmódosítást elszenvedő felhasználó egyedi azonosítója
	 * @nodeId csomópont azonosítója
	 * @roleCode legmagasabb jogkör neve.
	 */
	@Override
	public ResponseEntity<?> setUserRoleForNode(String token, Long userId, Long nodeId, String roleCode) {
		Long currentUserId = jwtProvider.getUserIdFromToken(token);
		if (!userHasRoleInHierarchy(currentUserId, UserNodeRoles.ADMIN_ROLE, nodeId)) {
			throw new CustomMessageException("ACCESS DENIED. Admin role requried to grant roles!",
					HttpStatus.FORBIDDEN);
		}
		
		Node currentNode = nodeRepository.getById(nodeId);
		grantRoleToUser(userId, roleCode, currentNode);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private void grantRoleToUser(Long userId, String roleCode, Node currentNode ) {
		nodeRepository.deleteAllRoleByUserByNode(userId, currentNode.getId());
		addAllRolesUpUntilHierarchyMatch(userId, roleCode, currentNode.getId());
		
		if (!isLeaf(currentNode)) {
			currentNode.getChildren().forEach(x-> grantRoleToUser(userId, roleCode, x));
		}
	}

	/**
	 * UserNodeRoles hierarhia szerinti összes jog kiosztása az adott felhasználonak, 
	 * egészen a paraméterként kapott jogig.
	 * @param userId 
	 * @param roleCode
	 * @param nodeId
	 */
	private void addAllRolesUpUntilHierarchyMatch(Long userId, String roleCode, Long nodeId) {
		int i  = 0; 
		while (!roleCode.equals(UserNodeRoles.ROLE_HIERARCHY.get(i))) {
			insertRoleUserRelation(userId, UserNodeRoles.ROLE_HIERARCHY.get(i), nodeId);
			++ i;
		}
		insertRoleUserRelation(userId, UserNodeRoles.ROLE_HIERARCHY.get(i), nodeId);
		
	}

	private void insertRoleUserRelation(Long userId, String roleCode, Long nodeId) {
		Long nodeRoleId = nodeRolesRepository.getNodeRoleIdByRoleCode(roleCode);
		nodeRepository.saveUserNodeRelation(userId, nodeId, nodeRoleId);
	}

	@Override
	public ResponseEntity<List<String>> getUserNodeRoles(String token, Long userId, Long nodeId) {
		if(!userService.userHasProjectRole(token, ProjectRoles.ROLE_PROJECT_ADMIN)) {
			throw new UserRequestNotAllowedException();
		}
		List<String> userNodeRoleList = nodeRepository.getUserNodeRoles(userId, nodeId).stream()
																		.map(nodeRole -> nodeRole.substring(5))
																		.collect(Collectors.toList());
		return new ResponseEntity<List<String>>(userNodeRoleList, HttpStatus.OK);
	}
}
