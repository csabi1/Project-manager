package hu.econsult.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
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

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "nodes")
public class Node implements Serializable{
	
	private static final long serialVersionUID = 5458683336278920638L;

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "project_name")
	String projectName;
	
	@Column(name = "node_description")
	String nodeDescription;
	
	@Column(name = "node_name")
	String nodeName;
	
	@JsonIgnore
	@CreationTimestamp
	@Column(name = "created_on")
	LocalDateTime createdOn;
	
	@Column(name = "is_file_support")
	Boolean isFileSupport;
	
	@Column(name = "is_active")
	Boolean isActive;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name="parent_child", joinColumns={@JoinColumn(name="parent_id")}, inverseJoinColumns={@JoinColumn(name="child_id")})
	private Set<Node> children;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "users_nodes", 
		joinColumns = {	@JoinColumn(name = "node_id", referencedColumnName="id") },
		inverseJoinColumns = { @JoinColumn(name = "user_id", referencedColumnName="id")
		})
	private List<User> users;
	
	public void addUser(User user) {
	if (this.users == null || this.users.isEmpty()) {
			this.users = new LinkedList<>();
		}
		this.users.add(user);
	}
	
	public List<User> getUsers(){
		if (this.users == null) {
			this.users = new LinkedList<>();
		}
		return this.users;
	}
	
}

