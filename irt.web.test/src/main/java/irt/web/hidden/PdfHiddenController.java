package irt.web.hidden;

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

import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.IpAddress;
import irt.web.service.IpService;

@RestController
@RequestMapping("pdf/hidden")
public class PdfHiddenController {
	private final static Logger logger = LogManager.getLogger();

	@Autowired private IpService ipService;

	@Value("${irt.web.root.path}")
	private String root;

	@Value("${irt.web.product.pdf.path}")
	private String productPdfPath;

	private Path pdfFolder;

	@PostConstruct
	public void postConstruct() {

		pdfFolder = Paths.get(root, productPdfPath);
	}

	@PostMapping("product/list")
	public List<String> getPdfList(@RequestParam Long productId) throws IOException{
		logger.traceEntry("productIdL {}", productId);

		final Path path = pdfFolder.resolve(productId.toString());

		if(path.toFile().exists()) {

			try(final Stream<Path> stream = Files.find(path, Integer.MAX_VALUE, (filePath, fileAttr)->fileAttr.isRegularFile() && filePath.toString().toLowerCase().endsWith(".pdf"));){
				return stream
						.map(pdfFolder::relativize)
						.map(Path::toString)
						.map(s->s.replaceAll("\\\\", "/"))
						.collect(Collectors.toList());
			}
		}
		return new ArrayList<>();
	}

	@PostMapping(path="/product/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String addPDF(@CookieValue(required = false) String clientIP, @RequestParam Long productId, @RequestPart MultipartFile file) {
		logger.traceEntry("clientIP: {}; productId: {};", clientIP, productId);

		final Optional<IpAddress> oRemoteAddress = ipService.getIpAddress(clientIP);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			logger.info("You are not authorized to perform this action.");
			return "You are not authorized to perform this action. (80)";
		}

		try {

			savePDF(productId, file, Long.toString(System.currentTimeMillis()));


		} catch (IllegalStateException | IOException e) {
			 logger.catching(e); 
			 return e.getLocalizedMessage();
		}

		return"Done";
	}

	@PostMapping("delete")
	public String deletePDF(@CookieValue(required = false) String clientIP, @RequestParam Path path) throws IOException{
		logger.traceEntry("path: {};", path);

		final Optional<IpAddress> oRemoteAddress = ipService.getIpAddress(clientIP);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			logger.info("You are not authorized to perform this action.");
			return "You are not authorized to perform this action. (102)";
		}

		Path p = pdfFolder.resolve(path);
		logger.error(p);

		final File file = p.toFile();

		if(!file.exists())
			return "There are no files to delete.";

		if(file.delete())
			return "Deleted";
		else
			return "Failed to delete the file.";
	}

	private void savePDF(Long productId, MultipartFile mpFile, String subfolderName) throws IllegalStateException, IOException {

		String originalFilename = mpFile.getOriginalFilename();
		Path path = pdfFolder.resolve(Paths.get(productId.toString(), subfolderName));

		logger.trace("productId: {}; path: {}", productId, path);

		path.toFile().mkdirs();	//create a directories

		path = Paths.get(path.toString(), originalFilename);

		mpFile.transferTo(path);
	}
}
