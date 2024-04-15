package irt.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("images")
public class ImagesController {
	private final static Logger logger = LogManager.getLogger();

	@Value("${irt.web.root.path}")
	private String root;

	@Value("${irt.web.product.images.path}")
	private String productImagesPath;

	@Value("${irt.web.home.slider.images.path}")
	private String homeSliderImagesPath;

	@Value("${irt.web.product.images.default}")
	private String defaultImage;

	@Value("${irt.web.event.images.path}")
	private String eventImagesPath;

	private Path imagesFolder;
	private Path imagesSliderFolder;
	private Path eventrFolder;

	@PostConstruct
	public void postConstruct() {

		imagesFolder = Paths.get(root, productImagesPath);
		imagesSliderFolder = Paths.get(root, homeSliderImagesPath);
		eventrFolder = Paths.get(root, eventImagesPath);
	}

	@GetMapping("product/{productId}")
	public ResponseEntity<Resource> getImage(@PathVariable Long productId) throws IOException{
		logger.traceEntry("productId: {};", productId);

		return imageById(imagesFolder, productId);
	}

	@GetMapping("product")
	public ResponseEntity<Resource> getImageByPath(@RequestParam Path path) throws IOException{
		logger.traceEntry("path: {}", path);

		final Path p = imagesFolder.resolve(path);
		return getImage(p);
	}

	@GetMapping("home/slider/{id}")
	public ResponseEntity<Resource> sliderImage(@PathVariable  Long id) throws IOException{
		logger.traceEntry("id: {};", id);

		return imageById(imagesSliderFolder, id);
	}

	@GetMapping("event/{eventId}")
	public ResponseEntity<Resource> getEventImage(@PathVariable Long eventId) throws IOException{
		logger.traceEntry("productId: {};", eventId);

		return imageById(eventrFolder, eventId);
	}

	private ResponseEntity<Resource> imageById(final Path imagesPath, Long productId) throws FileNotFoundException, IOException {

		final Path path = imagesPath.resolve(productId.toString());

		if(!path.toFile().exists())
			return defaultImage();

		try(final Stream<Path> stream = Files.walk(path).filter(Files::isRegularFile);){
			final Optional<Path> oPath = stream.findAny();
			logger.debug(oPath);

			return oPath.isPresent() ? getImage(oPath.get()) : defaultImage();
		}
	}

	private ResponseEntity<Resource> defaultImage() {

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=\"no_photo.jpg\"");

		InputStream is = getClass().getResourceAsStream("/static/images/jpeg/no_photo.jpg");
		return ResponseEntity.ok()
				.headers(headers)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(is));
	}

	private ResponseEntity<Resource> getImage(final Path path) throws FileNotFoundException {
		logger.traceEntry("{}", path);
		final File file = path.toFile();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");

		if(!file.exists()) {
			return ResponseEntity.notFound()
					.headers(headers)
					.build();
		}

		final InputStream is = new FileInputStream(file);

		return ResponseEntity.ok()
				.headers(headers)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(is));
	}
}
