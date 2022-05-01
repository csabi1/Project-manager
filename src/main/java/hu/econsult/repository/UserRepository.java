package hu.econsult.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hu.econsult.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@Query(value="SELECT * FROM users WHERE id = :userId", nativeQuery = true)
	Optional<User> findById(@Param("userId") Long userId);

	Optional<User> findByUsername(String username);

	@Query(value = "select exists (select true from users_nodes_roles where user_id = ?1 and node_id = ?2 and node_role_id = (select node_role_id from node_roles where node_role = ?3))", nativeQuery = true)
	boolean userHasGivenRole(Long userId, Long nodeId, String roleCode);
}
