package hu.econsult.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import hu.econsult.model.entity.NodeDataFile;

@Repository
public interface NodeDataFileRepository extends JpaRepository<NodeDataFile, Long> {

	void deleteByNodeId(Long id);

	void deleteByFileId(Long fileId);
	
	@Query(value = "select node_id from multipart_files where file_id = ?1", nativeQuery = true)
	Long getNodeIdByFileId(Long id);

	Optional<NodeDataFile> findById(Long fileId);

	Optional<List<NodeDataFile>> findByUploaderUserId(Long currentUserId);

	Optional<List<NodeDataFile>> findByNodeId(Long nodeId);
}
