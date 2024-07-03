package irt.web.controllers.hidden;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import irt.web.bean.FileWorker;
import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.IpAddress;
import irt.web.service.IpService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("hidden/files")
public class FileHiddenController extends FileWorker {

	@Value("${irt.web.root.path}")
	private String root;

	@Value("${irt.web.files.path}")
	private String filesPath;

	@Autowired private IpService ipService;

	private Path filesFolder;

	@PostConstruct
	public void postConstruct() {
		filesFolder = Paths.get(root, filesPath);
	}

	@PostMapping(path="upload/gui", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String uploadGui(HttpServletRequest request, @RequestParam String folder, @RequestPart MultipartFile file){
		final String remoteAddr = request.getRemoteAddr();
		logger.traceEntry("clientIP: {}; folder: {};", remoteAddr, folder);

		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr);

		if(!oIpAddress.filter(addr->addr.getTrustStatus()==TrustStatus.IRT).isPresent()) {
			logger.warn("Not authorized to upload: {}", oIpAddress);
			return "You are not authorized to perform this action.";
		}

		try {

			final Path path = filesFolder.resolve(folder);
			FileUtils.cleanDirectory(path.toFile());
			saveFile(path, file);

		} catch (IllegalStateException | IOException e) {
			 logger.catching(e); 
			 return e.getLocalizedMessage();
		}

		return"Done";
	}

	@PostMapping(path="upload/doc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String uploadDoc(HttpServletRequest request, @RequestParam String folder, @RequestParam String subfolder, @RequestPart MultipartFile file){
		final String remoteAddr = request.getRemoteAddr();
		logger.traceEntry("clientIP: {}; folder: {}; subfolder: {}", remoteAddr, folder, subfolder);

		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr);

		if(!oIpAddress.filter(addr->addr.getTrustStatus()==TrustStatus.IRT).isPresent()) {
			logger.warn("Not authorized to upload: {}", oIpAddress);
			return "You are not authorized to perform this action.";
		}

		try {

			final Path path = filesFolder.resolve(folder, subfolder);
			path.toFile().mkdirs();
			saveFile(path, file);

		} catch (IllegalStateException | IOException e) {
			 logger.catching(e); 
			 return e.getLocalizedMessage();
		}

		return"Done";
	}

	@PostMapping(path="rename/**")
	public String rename(@RequestParam String renameTo, HttpServletRequest request) throws IOException{
		final String remoteAddr = request.getRemoteAddr();
		logger.traceEntry("clientIP: {}; renameTo: {};", remoteAddr, renameTo);

		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr);

		if(!oIpAddress.filter(addr->addr.getTrustStatus()==TrustStatus.IRT).isPresent()) {
			logger.warn("Not authorized to upload: {}", oIpAddress);
			return "You are not authorized to perform this action.";
		}

		final String pathFromUrl = ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replaceFirst("/hidden/files/rename/", "");
		final String decoded = URLDecoder.decode(pathFromUrl, StandardCharsets.UTF_8);
		final Path pOriginal = filesFolder.resolve(decoded);
		final File fOriginal = pOriginal.toFile();

		if(!fOriginal.exists()) 
			return "File " + fOriginal + " not found.";

		final Path pNewName = pOriginal.getParent().resolve(renameTo);
		final File fNewName = pNewName.toFile();

		if(fNewName.exists()) 
			return "A file with the same name already exists";

		final Path moved = Files.move(pOriginal, pNewName);

		return moved.equals(pNewName) ? "Done" : "Something went wrong.\nThe file has not been modified.";
	}

	@DeleteMapping(path="delete/**")
	public String delete(HttpServletRequest request) throws IOException{
		final String remoteAddr = request.getRemoteAddr();
		logger.traceEntry("clientIP: {};", remoteAddr);

		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr);

		if(!oIpAddress.filter(addr->addr.getTrustStatus()==TrustStatus.IRT).isPresent()) {
			logger.warn("Not authorized to upload: {}", oIpAddress);
			return "You are not authorized to perform this action.";
		}

		final String pathFromUrl = ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replaceFirst("/hidden/files/delete/", "");
		final String decoded = URLDecoder.decode(pathFromUrl, StandardCharsets.UTF_8);
		final Path pOriginal = filesFolder.resolve(decoded);

		Files.delete(pOriginal);

		return "Done";
	}
}
