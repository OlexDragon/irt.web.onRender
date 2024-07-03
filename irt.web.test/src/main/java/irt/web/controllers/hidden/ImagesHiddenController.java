package irt.web.controllers.hidden;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("images/hidden")
public class ImagesHiddenController extends FileWorker {
	private final static Logger logger = LogManager.getLogger();

	@Autowired private IpService ipService;

	@Value("${irt.web.root.path}")
	private String root;

	@Value("${irt.web.product.images.path}")
	private String productImagesPath;

	@Value("${irt.web.home.slider.images.path}")
	private String sliderImagesPath;

	@Value("${irt.web.event.images.path}")
	private String eventImagesPath;

	private Path imagesFolder;
	private Path sliderFolder;
	private Path eventrFolder;

	@PostConstruct
	public void postConstruct() {

		imagesFolder = Paths.get(root, productImagesPath);
		sliderFolder = Paths.get(root, sliderImagesPath);
		eventrFolder = Paths.get(root, eventImagesPath);
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

	@PostMapping(path="product/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String addOneImages(HttpServletRequest request, @RequestParam Long productId, @RequestPart MultipartFile file) {
		final String remoteAddr = request.getRemoteAddr();
		logger.traceEntry("clientIP: {}; productId: {};", remoteAddr, productId);

		final Optional<IpAddress> oRemoteAddress = ipService.getIpAddress(remoteAddr);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			logger.info("You are not authorized to perform this action.");
			return "You are not authorized to perform this action. (93)";
		}

		try {
			
			final Path folder = imagesFolder.resolve(Paths.get(productId.toString(), Long.toString(System.currentTimeMillis())));
			saveFile(folder, file);


		} catch (IllegalStateException | IOException e) {
			 logger.catching(e); 
			 return e.getLocalizedMessage();
		}

		return"Done";
	}

	@PostMapping("delete")
	public String deleteImage(HttpServletRequest request, @RequestParam Path path) throws IOException{
		final String remoteAddr = request.getRemoteAddr();
		logger.traceEntry("remoteAddr: {}; path: {};", path);

		final Optional<IpAddress> oRemoteAddress = ipService.getIpAddress(remoteAddr);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			logger.info("You are not authorized to perform this action.");
			return "You are not authorized to perform this action. (116)";
		}

		Path p = imagesFolder.resolve(path);
		logger.debug(p);

		final File file = p.toFile();

		if(!file.exists())
			return "There are no files to delete.";

		if(file.delete())
			return "Deleted";
		else
			return "Failed to delete the file.";
	}

	@PostMapping(path="carousel/card/add/{cardId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String setCarouselImage(

			HttpServletRequest request,
			@PathVariable Long cardId,
			@RequestPart MultipartFile file) {
		final String remoteAddr = request.getRemoteAddr();

		logger.traceEntry("cardId: {}; clientIP: {}", cardId, remoteAddr);

		final Optional<IpAddress> oRemoteAddress = ipService.getIpAddress(remoteAddr);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			logger.info("You are not authorized to perform this action.");
			return "You are not authorized to perform this action. (144)";
		}

		final Path path = sliderFolder.resolve(cardId.toString());
		logger.debug(path);

		final File directory = path.toFile();

		try {

			if(!directory.mkdirs())
				FileUtils.cleanDirectory(directory);

			String originalFilename = file.getOriginalFilename();
			final Path p = path.resolve(originalFilename);
			file.transferTo(p);

		} catch (IllegalStateException | IOException e) {
			 logger.catching(e); 
			 return e.getLocalizedMessage();
		}

		return "Done";
	}

	@PostMapping(path="event/add/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String setEventImage(

			HttpServletRequest request,
			@PathVariable Long eventId,
			@RequestPart MultipartFile file) {
		final String remoteAddr = request.getRemoteAddr();

		logger.traceEntry("cardId: {}; clientIP: {}", eventId, remoteAddr);

		final Optional<IpAddress> oRemoteAddress = ipService.getIpAddress(remoteAddr);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			logger.info("You are not authorized to perform this action.");
			return "You are not authorized to perform this action. (180)";
		}

		final Path path = eventrFolder.resolve(eventId.toString());
		logger.debug(path);

		final File directory = path.toFile();

		try {

			if(!directory.mkdirs())
				FileUtils.cleanDirectory(directory);

			String originalFilename = file.getOriginalFilename();
			final Path p = path.resolve(originalFilename);
			file.transferTo(p);

		} catch (IllegalStateException | IOException e) {
			 logger.catching(e); 
			 return e.getLocalizedMessage();
		}

		return "Done";
	}
}
