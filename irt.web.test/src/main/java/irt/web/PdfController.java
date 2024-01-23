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
@RequestMapping("pdf")
public class PdfController {
	private final static Logger logger = LogManager.getLogger();

	private String home = System.getProperty("user.home");

	@Value("${irt.web.product.pdf.path}")
	private String productPdfPath;

	private Path pdfFolder;

	@PostConstruct
	public void postConstruct() {

		pdfFolder = Paths.get(home, productPdfPath);
	}

	@GetMapping("product/{productId}")
	public ResponseEntity<Resource> getPdf(@PathVariable Long productId) throws IOException{
		logger.traceEntry("productId: {};", productId);

		return imageById(productId);
	}

	@GetMapping("product")
	public ResponseEntity<Resource> getPdfByPath(@RequestParam Path path) throws IOException{
		logger.traceEntry("path: {}", path);

		final Path p = pdfFolder.resolve(path);
		return getPdf(p);
	}

	private ResponseEntity<Resource> imageById(Long productId) throws IOException {

		final Path path = pdfFolder.resolve(productId.toString());

		if(!path.toFile().exists())
			return notFound();

		try(final Stream<Path> stream = Files.walk(path).filter(Files::isRegularFile);){
			final Optional<Path> oPath = stream.findAny();

			return oPath.isPresent() ? getPdf(oPath.get()) : notFound();
		}
	}

	private ResponseEntity<Resource> notFound() {
		return ResponseEntity.notFound()
				.headers(getHeader())
				.build();
	}

	private ResponseEntity<Resource> getPdf(final Path path) throws FileNotFoundException {
		final File file = path.toFile();

		HttpHeaders headers = getHeader();
		headers.add("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");

		if(!file.exists())
			return notFound();

		final InputStream is = new FileInputStream(file);

		return ResponseEntity.ok()
				.headers(headers)
				.contentType(MediaType.APPLICATION_PDF)
				.body(new InputStreamResource(is));
	}

	private HttpHeaders getHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");
		return headers;
	}
}
