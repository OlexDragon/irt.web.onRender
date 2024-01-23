package irt.web.hidden;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import irt.web.bean.jpa.RemoteAddress;
import irt.web.bean.jpa.RemoteAddress.TrustStatus;
import irt.web.bean.jpa.RemoteAddressRepository;

@RestController
@RequestMapping("images/hidden")
public class ImagesHiddenController {
	private final static Logger logger = LogManager.getLogger();

	@Autowired private RemoteAddressRepository	 remoteAddressRepository;

	private String home = System.getProperty("user.home");

	@Value("${irt.web.product.images.path}")
	private String productImagesPath;

	private Path imagesFolder;

	@PostConstruct
	public void postConstruct() {

		imagesFolder = Paths.get(home, productImagesPath);
	}

	@PostMapping("product/list")
	public List<String> getImageList(@RequestParam Long productId) throws IOException{
		logger.traceEntry("productIdL {}", productId);

		final Path path = imagesFolder.resolve(productId.toString());

		if(path.toFile().exists()) {

			try(final Stream<Path> stream = Files.find(path, Integer.MAX_VALUE, (filePath, fileAttr)->fileAttr.isRegularFile());){
				return stream
						.map(imagesFolder::relativize)
						.map(Path::toString)
						.map(s->s.replaceAll("\\\\", "/"))
						.collect(Collectors.toList());
			}
		}
		return new ArrayList<>();
	}

	@PostMapping(path="/product/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String addOneImages(@CookieValue(required = false) String clientIP, @RequestParam Long productId, @RequestPart MultipartFile file) {
		logger.traceEntry("clientIP: {}; productId: {};", clientIP, productId);

		final Optional<RemoteAddress> oRemoteAddress = Optional.ofNullable(clientIP).map(this::getRemoteAddress);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) 
			return "You are not authorized to perform this action.";

		try {

			saveImage(productId, file, Long.toString(System.currentTimeMillis()));


		} catch (IllegalStateException | IOException e) {
			 logger.catching(e); 
			 return e.getLocalizedMessage();
		}

		return"Done";
	}

	@PostMapping("delete")
	public String deleteImage(@CookieValue(required = false) String clientIP, @RequestParam Path path) throws IOException{
		logger.traceEntry("path: {};", path);

		final Optional<RemoteAddress> oRemoteAddress = Optional.ofNullable(clientIP).map(this::getRemoteAddress);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) 
			return "You are not authorized to perform this action.";

		Path p = imagesFolder.resolve(path);
		logger.error(p);

		final File file = p.toFile();

		if(!file.exists())
			return "There are no files to delete.";

		if(file.delete())
			return "Deleted";
		else
			return "Failed to delete the file.";
	}

	private void saveImage(Long productId, MultipartFile mpFile, String subfolderName) throws IllegalStateException, IOException {

		String originalFilename = mpFile.getOriginalFilename();
		Path path = imagesFolder.resolve(Paths.get(productId.toString(), subfolderName));

		logger.trace("productId: {}; path: {}", productId, path);

		path.toFile().mkdirs();	//create a directories

		path = Paths.get(path.toString(), originalFilename);

		mpFile.transferTo(path);
	}

	private RemoteAddress getRemoteAddress(final String remoteAddr) {

		return remoteAddressRepository.findById(remoteAddr)

				.map(
						ra->{
							final LocalDateTime now = LocalDateTime.now();
							ra.setConnectionCount(ra.getConnectionCount()+1);
							ra.setLastConnection(now);
							return remoteAddressRepository.save(ra);
						})
				.orElseGet(
						()->{
							final LocalDateTime now = LocalDateTime.now();
							return remoteAddressRepository.save(new RemoteAddress(remoteAddr, now, now, 1, TrustStatus.UNKNOWN));
						});
	}
}
