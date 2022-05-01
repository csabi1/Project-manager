package hu.econsult.service;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import hu.econsult.message.ResponseFile;
import hu.econsult.message.ResponseMessage;
import hu.econsult.model.entity.NodeDataFile;

public interface FileStorageService {

	public ResponseEntity<ResponseMessage> store(String token, MultipartFile file, Long nodeId);

	public ResponseEntity<byte[]> getFileById(Long id);

	public NodeDataFile getFile(Long id);

	public ResponseEntity<List<ResponseFile>> getFilesList(String token);

	public ResponseEntity<List<ResponseFile>> getNodeFilesList(String token, Long nodeId);

	public Stream<NodeDataFile> getAllFiles();

	public ResponseEntity<?> deleteFile(String token, Long id);
	
	//public void deleteFile(String token, Long id);

	public void deleteFilesOfNodes(Long id);

	public ResponseEntity<List<ResponseFile>> getFilesUploadedByUser(String token);
}
