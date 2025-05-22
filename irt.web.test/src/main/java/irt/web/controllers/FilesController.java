package irt.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import irt.web.bean.ConnectTo;
import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.IpAddress;
import irt.web.bean.jpa.IpConnection;
import irt.web.service.IpService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("files")
public class FilesController {
	private final Logger logger = LogManager.getLogger();

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

	@GetMapping("get/**")
	public ResponseEntity<InputStreamResource> get(HttpServletRequest request) throws FileNotFoundException {

		final String pathFromUrl = ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replaceFirst("/files/get/", "");
		final String decoded = URLDecoder.decode(pathFromUrl, StandardCharsets.UTF_8);

		HttpHeaders headers = new HttpHeaders();
		Path filePath = filesFolder.resolve(decoded);
		final String fileName = filePath.getFileName().toString();

		final File file = filePath.toFile();
		final InputStream is;
		final String extension;
		final BodyBuilder bodyBuilder;

		if(file.exists()) {

			is = new FileInputStream(file);
			extension = FilenameUtils.getExtension(fileName);
			headers.add("Content-Disposition", "inline; filename=\"" + fileName + "\"");
			bodyBuilder = ResponseEntity.ok().headers(headers);

		}else {

			final String string = "The file " + fileName + " does not exist.";
			is = new ByteArrayInputStream(string.getBytes());
			extension = "txt";
			final String baseName = FilenameUtils.getBaseName(fileName);
			headers.add("Content-Disposition", "inline; filename=\"" + baseName + ".txt\"");
			logger.warn("The file '" + file + "' does not exist.");
			bodyBuilder = ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers);
		}


		final BodyBuilder contentType;

		switch(extension) {

		case "pdf":
			contentType = bodyBuilder.contentType(MediaType.APPLICATION_PDF);
			break;

		case "txt":
			contentType = bodyBuilder.contentType(MediaType.TEXT_PLAIN);
			break;

		default:
			contentType = bodyBuilder.contentType(MediaType.APPLICATION_OCTET_STREAM);
		}

		return contentType.body(new InputStreamResource(is));
	}

	@GetMapping("gui/{version}")
	public ResponseEntity<InputStreamResource> getGui(@PathVariable Integer version,  HttpServletRequest request) throws IOException{
		final String remoteAddr = Optional.ofNullable(request.getHeader( "X-Forwarded-For" )).orElseGet(()->request.getRemoteAddr());
		logger.info("RemoteAddre: {}", remoteAddr);

		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr).filter(ip->ip.getTrustStatus()!=TrustStatus.NOT_TRUSTED);

		if(!oIpAddress.isPresent()) {
			logger.warn("{} - Downloading is prohibited. You are blacklisted.", remoteAddr);
			return downloadProhibited("Downloading is prohibited. You are blacklisted.", HttpStatus.BAD_REQUEST);
		}

		final LocalDateTime now = LocalDateTime.now(ZoneId.of("Canada/Eastern"));
		final LocalDateTime monthAgo = now.minusMonths(1);

		final IpAddress ipAddress = oIpAddress.get();
		final List<IpConnection> connections = ipService.getConnections(ipAddress.getId(), ConnectTo.GUI, monthAgo);
		final InputStream is;

		if(connections.size()>100 && ipAddress.getTrustStatus()==TrustStatus.UNKNOWN) {

			logger.warn("{} - Downloading is prohibited. You are blacklisted.", remoteAddr);
			ipAddress.setTrustStatus(TrustStatus.NOT_TRUSTED);
			ipService.save(ipAddress);
			return downloadProhibited("Downloading is prohibited. You are blacklisted.", HttpStatus.BAD_REQUEST);
		}

		final Optional<Long> oWait = connections.parallelStream().max(Comparator.comparing(IpConnection::getDate)).map(ic->ChronoUnit.MINUTES.between(ic.getDate(), now)).filter(l->l<15).map(l->15-l);
		if(oWait.isPresent()) {
			final String string = "The next download can be done in " + oWait.get() + " minutes.";
			logger.info(string);
			return downloadProhibited(string, HttpStatus.LOCKED);
		}

		try(final Stream<Path> stream = Files.walk(filesFolder.resolve("gui" + (version ==3 ? "" : version))).filter(Files::isRegularFile);){

			final Optional<Path> oPath = stream.findAny();
			logger.debug(oPath);

			if(oPath.isPresent()) {
				ipService.createConnection(ipAddress.getId(), ConnectTo.GUI);
				final Path filePath = oPath.get();

				HttpHeaders headers = new HttpHeaders();
				headers.add("Content-Disposition", "attachment; filename=\"" + filePath.getFileName() + "\"");

				is = new FileInputStream(filePath.toFile());

				return ResponseEntity.ok()

						.headers(headers)
						.contentType(MediaType.APPLICATION_OCTET_STREAM)
						.body(new InputStreamResource(is));
			}
		}

		return downloadProhibited("Sorry, GUI file not found.", HttpStatus.NOT_FOUND);
	}

	private ResponseEntity<InputStreamResource> downloadProhibited(final String string, HttpStatusCode status) {
		final InputStream is;
		is = new ByteArrayInputStream(string.getBytes());
		final HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=\"gui.txt\"");
		return ResponseEntity.status(status).headers(headers).contentType(MediaType.TEXT_PLAIN).body(new InputStreamResource(is));
	}
}
