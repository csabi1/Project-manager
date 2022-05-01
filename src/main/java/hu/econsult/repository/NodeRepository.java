package hu.econsult.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hu.econsult.model.entity.Node;

@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {
	
	@Override
	public Node getById(Long id);
	
	@Transactional
	@Modifying
	@Query(value = "DELETE FROM parent_child where child_id  = ?1", nativeQuery = true)
	public void deleteParentChildRelation(Long childId);

	@Query(value = "SELECT * FROM nodes where id not in (select child_id from parent_child) AND is_active = TRUE", nativeQuery = true)
	public List<Long> getAllProjectId();

	@Query(value = "select exists(select true from parent_child where child_id = ?1) ", nativeQuery = true)
	public boolean isChildNode(Long nodeId);

	@Query(value = "select * from nodes where id  = (select parent_id from parent_child where child_id  = ?1 ) limit 1", nativeQuery = true)
	public Node getParentNode(Long nodeProject);
	
	@Query(value = "SELECT EXISTS (SELECT true FROM users_nodes WHERE user_id =:userId AND node_id =:nodeId)", nativeQuery = true)
	public boolean isExistNodeWithUserId(@Param("userId") Long userId, @Param("nodeId") Long nodeId);

	@Query(value = "select * from nodes where id IN (select node_id from users_nodes where user_id = ?1) ", nativeQuery = true)
	public List<Node> getUsersProjectById(Long userId);

	@Transactional
	@Modifying
	@Query(value = "delete from users_nodes_roles where user_id = ?1 and node_id = ?2 ", nativeQuery = true)
	public void deleteAllRoleByUserByNode(Long userId, Long nodeId);

	@Transactional
	@Modifying
	@Query(value = "insert into users_nodes_roles values (?1, ?2, ?3)", nativeQuery = true)
	public void saveUserNodeRelation(Long userId, Long nodeId , Long nodeRoleId);

	@Transactional
	@Modifying
	@Query(value = "delete from userS_nodes_roles where node_id = ?1 ", nativeQuery = true)
	public void deleteAllRoleByNode(Long nodeId);
	
	
	@Query(value = "SELECT node_role FROM node_roles WHERE node_role_id IN (SELECT node_role_id FROM users_nodes_roles WHERE user_id = :userId AND node_id = :nodeId)", nativeQuery = true)
	public List<String> getUserNodeRoles(@Param("userId") Long userId, @Param("nodeId")Long nodeId);

	@Query(value = "select node_role from node_roles where node_role_id in (select node_role_id from users_nodes_roles where user_id = ?1 and node_id = ?2)", nativeQuery = true)
	public List<String> getAllNodeRoles(Long userId, Long nodeId);

}
