package hu.econsult.service.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import hu.econsult.exceptions.CustomMessageException;
import hu.econsult.exceptions.UserRequestNotAllowedException;
import hu.econsult.jwt.JwtProvider;
import hu.econsult.message.ResponseFile;
import hu.econsult.message.ResponseMessage;
import hu.econsult.model.entity.NodeDataFile;
import hu.econsult.model.entity.Node;
import hu.econsult.model.entity.User;

import hu.econsult.model.UserNodeRoles;
import hu.econsult.repository.NodeDataFileRepository;
import hu.econsult.repository.NodeRepository;
import hu.econsult.service.FileStorageService;
import hu.econsult.service.UserService;
import hu.econsult.utils.Utils;

@Service
public class FileStorageServiceImpl implements FileStorageService {

	private UserService userService;
	private JwtProvider jwtProvider;
	private final NodeRepository nodeRepository;
	private final NodeDataFileRepository nodeDataFileRepository;

	@Autowired
	public FileStorageServiceImpl(NodeDataFileRepository nodeDataFileRepository, UserService userService,
			JwtProvider jwtProvider, NodeRepository nodeRepository) {
		this.jwtProvider = jwtProvider;
		this.nodeRepository = nodeRepository;
		this.userService = userService;
		this.nodeDataFileRepository = nodeDataFileRepository;
	}

	@Override
	public ResponseEntity<ResponseMessage> store(String token, MultipartFile file, Long nodeId) {

		Long currentUserId = jwtProvider.getUserIdFromToken(token);
		if (!userService.userHasRole(currentUserId, UserNodeRoles.MODIFY_ROLE, nodeId)) {
			throw new CustomMessageException("Nincs joga a müvelethez!", HttpStatus.FORBIDDEN);
		}

		if (!isNodeFileSupportEnabled(nodeId)) {
			throw new CustomMessageException("Nem engedélyezett a fájlfeltöltés!", HttpStatus.BAD_REQUEST);
		}

		User user = jwtProvider.getUserFromToken(token);
		if (!nodeRepository.isExistNodeWithUserId(user.getId(), nodeId)) {
			throw new UserRequestNotAllowedException();
		}

		String message = "";
		try {	
			String originalFileName = file.getOriginalFilename();
			if (originalFileName == null) {
				throw new CustomMessageException("Az eredeti fálnév visszafejthetetlen!", HttpStatus.BAD_REQUEST);
			}
			
			String fileName = StringUtils.cleanPath(originalFileName);
			NodeDataFile fileDb = new NodeDataFile(fileName, file.getContentType(),
					Utils.byteArrayToBase64((file.getBytes())), nodeId, user.getId());
			nodeDataFileRepository.save(fileDb);

			message = "Fájl sikeresen feltöltve: " + file.getOriginalFilename();
			return new ResponseEntity<>(new ResponseMessage(message), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			message = "Fájl feltöltés sikertelen: " + file.getOriginalFilename() + "!";
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
		}
	}

	@Override
	public ResponseEntity<byte[]> getFileById(Long id) {
		NodeDataFile fileDb = getFile(id);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDb.getFileName() + "\"")
				.body(Utils.base64ToByteArray(fileDb.getContentBase64()));
	}

	/**
	 * Visszaad egy fájt id alapján.
	 */
	@Override
	public NodeDataFile getFile(Long id) {
		return nodeDataFileRepository.findById(id).orElseThrow(NoSuchElementException::new);
	}

	/**
	 * Visszaadja az összes feltöltött fájl listáját.
	 */
	@Override
	public ResponseEntity<List<ResponseFile>> getFilesList(String token) {
		List<ResponseFile> files = getAllFiles().map(dbFile -> {
			String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("api/file/")
					.path(dbFile.getFileId().toString()).toUriString();

			return new ResponseFile(dbFile.getFileName(), fileDownloadUri, dbFile.getContentType(),
					dbFile.getContentBase64().length());
		}).collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK).body(files);
	}

	/**
	 * Visszaadja a node-hoz feltöltött fájlok listáját.
	 */
	@Override
	public ResponseEntity<List<ResponseFile>> getNodeFilesList(String token, Long nodeId) {

		User user = jwtProvider.getUserFromToken(token);
		if (!nodeRepository.isExistNodeWithUserId(user.getId(), nodeId)) {
			throw new UserRequestNotAllowedException();
		}

		List<NodeDataFile> nodeFiles = getNodeAllFiles(nodeId);
		return ResponseEntity.status(HttpStatus.OK).body(getFileResponseFromEntity(nodeFiles));
	}

	@Override
	public ResponseEntity<List<ResponseFile>> getFilesUploadedByUser(String token) {
		Long currentUserId = jwtProvider.getUserIdFromToken(token);
		List<NodeDataFile> filesUploadedByUser = getFilesByUserId(currentUserId);
		return new ResponseEntity<>(getFileResponseFromEntity(filesUploadedByUser), HttpStatus.OK);
	}

	/**
	 * Adatbázisban tárolt entitásból ResponseFile-t csinál.
	 * 
	 * @param filesFromDatabase adatbázisban tárolt fájl lista
	 * @return
	 */
	private List<ResponseFile> getFileResponseFromEntity(List<NodeDataFile> filesFromDatabase) {
		return filesFromDatabase.stream().map(dbFile -> {
			String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("api/file/")
					.path(dbFile.getFileId().toString()).toUriString();
			return new ResponseFile(dbFile.getFileName(), fileDownloadUri, dbFile.getContentType(),
					dbFile.getContentBase64().length());
		}).collect(Collectors.toList());

	}

	private List<NodeDataFile> getFilesByUserId(Long currentUserId) {
		return nodeDataFileRepository.findByUploaderUserId(currentUserId).orElseThrow(NoSuchElementException::new);
	}

	@Override
	public Stream<NodeDataFile> getAllFiles() {
		return nodeDataFileRepository.findAll().stream();
	}

	public List<NodeDataFile> getNodeAllFiles(Long nodeId) {
		return nodeDataFileRepository.findByNodeId(nodeId).orElse(null);
	}

	@Override
	public void deleteFilesOfNodes(Long nodeId) {
		nodeDataFileRepository.deleteByNodeId(nodeId);
	}

	/**
	 * Engedélyezett-e a fájlfeltöltés a nodehoz.
	 * 
	 * @param nodeId
	 * @return
	 */
	private boolean isNodeFileSupportEnabled(Long nodeId) {
		Node node = nodeRepository.getById(nodeId);
		return node.getIsFileSupport();
	}

	@Override
	public ResponseEntity<?> deleteFile(String token, Long id) {
		Long currentUserId = jwtProvider.getUserIdFromToken(token);
		Long nodeId = nodeDataFileRepository.getNodeIdByFileId(id);
		if (!userService.userHasRole(currentUserId, UserNodeRoles.DELETE_ROLE, nodeId)) {
			throw new CustomMessageException("Nincs joga a müvelethez!", HttpStatus.FORBIDDEN);
		}
		nodeDataFileRepository.deleteById(id);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

}
