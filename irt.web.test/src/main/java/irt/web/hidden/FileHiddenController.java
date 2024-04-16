package irt.web.hidden;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import irt.web.bean.FileWorker;
import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.IpAddress;
import irt.web.service.IpService;
import jakarta.annotation.PostConstruct;

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

	@PostMapping(path="upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String upload(@CookieValue(required = false) String clientIP, @RequestParam String folder, @RequestPart MultipartFile file){
		logger.traceEntry("clientIP: {}; folder: {};", clientIP, folder);

		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(clientIP);

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
}
