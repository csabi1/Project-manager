package hu.econsult.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "multipart_files")
@AllArgsConstructor
@NoArgsConstructor
public class NodeDataFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "file_id")
	private Long fileId;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "content_type")
	private String contentType;

	@Column(name = "content_base64")
	private String contentBase64;
	
	@Column(name = "node_id")
	private Long nodeId;
	
	@Column(name = "uploader_user_id")
	private Long uploaderUserId;

	public NodeDataFile(String fileName, String type, String contentBase64, Long nodeId, Long uploaderUserId) {
		super();
		this.fileName = fileName;
		this.contentType = type;
		this.contentBase64 = contentBase64;
		this.nodeId = nodeId;
		this.uploaderUserId = uploaderUserId;
	}
	
}
