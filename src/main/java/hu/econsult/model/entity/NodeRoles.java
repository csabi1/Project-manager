package hu.econsult.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "node_roles")
public class NodeRoles implements Serializable{
	
	private static final long serialVersionUID = -9063145661912284600L;

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "node_role_id")
	private long nodeRoleId;

	@Column(name = "node_role", unique = true)
	private String nodeRole;
	
}
