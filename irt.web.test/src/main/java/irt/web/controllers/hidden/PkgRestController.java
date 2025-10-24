package irt.web.controllers.hidden;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
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
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("pkg")
public class PkgRestController extends FileWorker {

	@Autowired private IpService ipService;

	@Value("${irt.web.root.path}")
	private String root;
    @Value("${irt.web.update.path}")
    private String updateFolder;

	private Path pkgRoot;

    @PostConstruct
	public void postConstruct() {
    	pkgRoot = Paths.get(root, updateFolder);
	}

    @Scheduled(initialDelay = 30, fixedRate = 30, timeUnit = TimeUnit.DAYS) //30 days)
    public void removeOld(){

    	final long now = System.currentTimeMillis();

    	try {

    		Files.list(pkgRoot).forEach(
    				pkgDir->{
						try {
							final long modified = Files.getLastModifiedTime(pkgRoot).toMillis();
							final long diff = now - modified;
							final long days = TimeUnit.MILLISECONDS.toDays(diff);
							if (days > 30) {
								FileUtils.deleteDirectory(pkgDir.toFile());
								logger.info("Deleted package folder: {}. Last modified: {} days ago", pkgDir, days);
							}

						} catch (IOException e) {
							logger.catching(e);
						}
					});
		} catch (IOException e) {
			logger.catching(e);
		}
    }

	@PostMapping(path="upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String upload(HttpServletRequest request, @RequestParam String pkgName, @RequestPart(name = "files[]") List<MultipartFile> files){

		final String remoteAddr = Optional.ofNullable(request.getHeader( "X-Forwarded-For" )).orElseGet(()->request.getRemoteAddr());
		logger.info("clientIP: {}; pkgName: {}; files: {}", remoteAddr, pkgName, files);

		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr);

		if(!oIpAddress.filter(addr->addr.getTrustStatus()==TrustStatus.IRT).isPresent()) {
			logger.warn("Not authorized to upload: {}", oIpAddress);
			return "You are not authorized to perform this action.";
		}

		List<String> errors = new ArrayList<>();
		files.forEach(file -> {

			try {

				final Path pkgPath = pkgRoot.resolve(pkgName);
				Files.createDirectories(pkgPath);
				final Path filePath = pkgPath.resolve(file.getOriginalFilename());
				logger.info("Saving file to: {}", filePath);
				file.transferTo(filePath);

			} catch (IllegalStateException | IOException e) {
				logger.catching(e);
				errors.add(e.getLocalizedMessage());
			}
		});

		if (errors.isEmpty())
			return"Done";

		return String.join("\n", errors);
	}

	@PostMapping("delete")
	public String delete(HttpServletRequest request, Path path) {

		final String remoteAddr = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
				.orElseGet(() -> request.getRemoteAddr());
		logger.info("clientIP: {}; pkgName: {}", remoteAddr, path);

		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr);

		if (!oIpAddress.filter(addr -> addr.getTrustStatus() == TrustStatus.IRT).isPresent()) {
			logger.warn("Not authorized to delete: {}", oIpAddress);
			return "You are not authorized to perform this action.";
		}

		final Path pkgPath = pkgRoot.resolve(path);
		final Path parent = pkgPath.getParent();

		if (Files.exists(pkgPath))
		try {

			final long fileCount = Files.list(parent).filter(Files::isRegularFile).count();
			if (fileCount == 0)
				return "No files to delete.";
			else if (fileCount > 1) {
				if(pkgPath.toFile().delete())
					return "Done";
			}else {
				FileUtils.deleteDirectory(parent.toFile());
				return "Folder";
			}

		} catch (IOException e) {
			logger.catching(e);
			return e.getLocalizedMessage();
		}

		return "File/Folder does not exist.";
	}

	@RequestMapping("list")
	public List<String> listPackages(@RequestParam(name = "f") String folderName) {
		List<String> packages = new ArrayList<>();
		final Path dir = pkgRoot.resolve(folderName);
		if (Files.exists(dir))
		try {
			Files.list(dir).forEach(path -> packages.add(path.getFileName().toString()));
		} catch (IOException e) {
			logger.catching(e);
		}
		return packages;
	}

	@PostMapping("load")
	public byte[] loadPackage(@RequestParam(name = "f") String folderName, @RequestParam(name = "p") String pkgName) {

		try {

			final Path pkgPath = pkgRoot.resolve(folderName).resolve(pkgName);
			if(Files.exists(pkgPath))
				return Files.readAllBytes(pkgPath);

		} catch (IOException e) {
			logger.catching(e);
		}
		return new byte[0];
	}
}
