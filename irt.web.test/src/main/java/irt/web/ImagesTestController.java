package irt.web;

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
public class ImagesTestController {
	private final static Logger logger = LogManager.getLogger();

	private String home = System.getProperty("user.home");

	@Value("${irt.web.product.images.path}")
	private String productImagesPath;

	@Value("${irt.web.product.images.default}")
	private String defaultImage;

	private Path imagesFolder;

	@PostConstruct
	public void postConstruct() {

		imagesFolder = Paths.get(home, productImagesPath);
	}

	@GetMapping("product/{productId}")
	public ResponseEntity<Resource> getImage(@PathVariable Long productId) throws IOException{
		logger.traceEntry("productId: {};", productId);

		return imageById(productId);
	}

	@GetMapping("product")
	public ResponseEntity<Resource> getImageByPath(@RequestParam Path path) throws IOException{
		logger.traceEntry("path: {}", path);

		final Path p = imagesFolder.resolve(path);
		return getImage(p);
	}

	private ResponseEntity<Resource> imageById(Long productId) throws IOException {

		final Path path = imagesFolder.resolve(productId.toString());

		if(!path.toFile().exists())
			return defaultImage();

		try(final Stream<Path> stream = Files.walk(path).filter(Files::isRegularFile);){
			final Optional<Path> oPath = stream.findAny();
			logger.debug(oPath);

			return oPath.isPresent() ? getImage(oPath.get()) : defaultImage();
		}
	}

	private ResponseEntity<Resource> defaultImage() {

		HttpHeaders headers = getHeader();
		headers.add("Content-Disposition", "attachment; filename=\"no_photo.jpg\"");

		InputStream is = getClass().getResourceAsStream("/static/images/jpeg/no_photo.jpg");
		return ResponseEntity.ok()
				.headers(headers)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(is));
	}

	private ResponseEntity<Resource> getImage(final Path path) throws FileNotFoundException {
		final File file = path.toFile();

		HttpHeaders headers = getHeader();
		headers.add("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

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

	private HttpHeaders getHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");
		return headers;
	}
}
