package hu.econsult.model.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "project_roles")
public class ProjectRole implements Serializable{

	private static final long serialVersionUID = -577255271401786310L;
	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "project_role_id")
	private long projectRoleId;

	@Column(name = "project_role", unique = true)
	private String projectRole;

	@JsonBackReference
	@ManyToMany(mappedBy = "projectRoles")
	private Set<User> users = new HashSet<User>();

	public ProjectRole(String projectRole) {
		this.projectRole = projectRole;
	}

	public Set<User> getUsers() {
		return users;
	}

	@Override
	public String toString() {
		return "ProjectRole [id=" + projectRoleId + ", ProjectRole=" + projectRole + "]";
	}

}
