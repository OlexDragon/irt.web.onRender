package irt.web.controllers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import irt.web.bean.ConnectTo;
import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.IpAddress;
import irt.web.bean.jpa.IpConnection;
import irt.web.service.IpService;
import jakarta.annotation.PostConstruct;

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

	@GetMapping("gui")
	public Object getGui(@CookieValue(required = false) String clientIP) throws IOException{
		logger.traceEntry("clientIP: {};", clientIP);

		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(clientIP).filter(ip->ip.getTrustStatus()!=TrustStatus.NOT_TRUSTED);

		if(!oIpAddress.isPresent()) {
			logger.warn("{} - Downloading is prohibited. You are blacklisted.", clientIP);
			return "Downloading is prohibited. You are blacklisted.";
		}

		final LocalDateTime now = LocalDateTime.now(ZoneId.of("Canada/Eastern"));
		final LocalDateTime monthAgo = now.minusMonths(1);

		final IpAddress ipAddress = oIpAddress.get();
		final List<IpConnection> connections = ipService.getConnections(ipAddress.getId(), ConnectTo.GUI, monthAgo);

		if(connections.size()>100 && ipAddress.getTrustStatus()==TrustStatus.UNKNOWN) {

			logger.warn("{} - Downloading is prohibited. You are blacklisted.", clientIP);
			ipAddress.setTrustStatus(TrustStatus.NOT_TRUSTED);
			ipService.save(ipAddress);
			return "Downloading is prohibited. You are blacklisted.";
		}

		final Optional<Long> oWait = connections.parallelStream().max(Comparator.comparing(IpConnection::getDate)).map(ic->ChronoUnit.MINUTES.between(ic.getDate(), now)).filter(l->l<15).map(l->15-l);
		if(oWait.isPresent())
			return "The next download can be done in " + oWait.get() + " minutes.";

		try(final Stream<Path> stream = Files.walk(filesFolder.resolve("gui")).filter(Files::isRegularFile);){

			final Optional<Path> oPath = stream.findAny();
			logger.debug(oPath);

			if(oPath.isPresent()) {
				ipService.createConnection(ipAddress.getId(), ConnectTo.GUI);
				final Path filePath = oPath.get();

				HttpHeaders headers = new HttpHeaders();
				headers.add("Content-Disposition", "attachment; filename=\"" + filePath.getFileName() + "\"");

				final InputStream is = new FileInputStream(filePath.toFile());

				return ResponseEntity.ok()

						.headers(headers)
						.contentType(MediaType.APPLICATION_OCTET_STREAM)
						.body(new InputStreamResource(is));
			}
		}

		return "Sorry, GUI file not found.";
	}
}
