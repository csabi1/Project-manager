package hu.econsult.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hu.econsult.model.entity.NodeRoles;

public interface NodeRolesRepository extends JpaRepository<NodeRoles, Long> {

	@Query(value ="select node_role_id from node_roles where node_role = ?1", nativeQuery = true)
	Long getNodeRoleIdByRoleCode(String roleCode);

}
