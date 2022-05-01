package hu.econsult.model.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Entity
@Data
@Table(name = "users",  schema = "public")
public class User implements Serializable{

	private static final long serialVersionUID = -7714107082776830604L;

	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	@Column(name = "id")
	private Long id;
	
	@Column(name = "name")
	@NotNull(message = "Nevet meg kell adni!")
	private String name;
	
	@Column(name = "username", unique = true)
	@NotNull(message = "Felhasználónevet meg kell adni!")
	private String username;
	
	@JsonIgnore
	@Column(name = "email", unique = true)
	@Email(message = "Valós e-mail címet adjon meg!")
	private String email;
	
	@JsonIgnore
	@Column(name = "password")
	@NotNull
	private String password;
	
	@JsonIgnore
	@Column(name = "enabled")
	private boolean enabled;
	
	@JsonIgnore
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "users_project_roles", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = {
			@JoinColumn(name = "project_role_id") })
	private Set<ProjectRole> projectRoles = new HashSet<ProjectRole>();
	
	
	
}
