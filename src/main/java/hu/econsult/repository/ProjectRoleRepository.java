package hu.econsult.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import hu.econsult.model.entity.ProjectRole;

@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {

	@Query(value="SELECT * FROM project_roles WHERE project_role = :rolename", nativeQuery = true)
	ProjectRole findByProjectRole(String rolename);

}
