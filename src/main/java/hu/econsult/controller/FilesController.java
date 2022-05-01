package hu.econsult.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hu.econsult.message.ResponseFile;
import hu.econsult.message.ResponseMessage;
import hu.econsult.service.FileStorageService;

@RestController
@RequestMapping("/api")
public class FilesController {
	
	@Autowired
	FileStorageService storageService;
	

	@PostMapping(value = "/uploads/{nodeId}",
			consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<ResponseMessage> uploadFile(@RequestHeader(name = "Authorization") String token, @RequestPart(value = "file") MultipartFile file,
														@PathVariable("nodeId") Long nodeId) {
		return storageService.store(token, file, nodeId);
	}
	
	
	@GetMapping("/files-list")
	public ResponseEntity<List<ResponseFile>> getFilesList(@RequestHeader(name = "Authorization") String token) {
		return storageService.getFilesList(token);
	}
	
	
	@GetMapping("/node-files-list/{nodeId}")
	public ResponseEntity<List<ResponseFile>> getNodeListFiles(@RequestHeader(name = "Authorization") String token,
			@PathVariable("nodeId") Long nodeId) {
		return storageService.getNodeFilesList(token, nodeId);
	}
	
	
	@GetMapping("/file/{fileId}")
	public ResponseEntity<byte[]> getFileById(@RequestHeader(name = "Authorization") String token,
			@PathVariable Long fileId) {		
		return storageService.getFileById(fileId);
	}
	
	@GetMapping("/files-uploaded-by-user")
	public ResponseEntity<List<ResponseFile>> getFilesUpdloadedByUser(@RequestHeader(name = "Authorization") String token) {
		return storageService.getFilesUploadedByUser(token);
	}
	
	@DeleteMapping("/delete-file/{fileId}")
	public ResponseEntity<?> deleteFile(@RequestHeader(name = "Authorization") String token, @PathVariable("fileId") Long fileId) {
		return storageService.deleteFile(token, fileId);
	}
	
	
}
