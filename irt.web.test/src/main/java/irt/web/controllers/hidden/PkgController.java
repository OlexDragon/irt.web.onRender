package irt.web.controllers.hidden;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.IpAddress;
import irt.web.service.IpService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/pkg")
public class PkgController implements ErrorController {
	private final Logger logger = LogManager.getLogger();

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

	@GetMapping
	public String pkg(HttpServletRequest request, Model model) {

		final String remoteAddr = Optional.ofNullable(request.getHeader( "X-Forwarded-For" )).orElseGet(()->request.getRemoteAddr());
		final Optional<IpAddress> oIpAddress = ipService.getIpAddress(remoteAddr);

		if(!oIpAddress.filter(addr->addr.getTrustStatus()==TrustStatus.IRT).isPresent()) {
			logger.warn("Not authorized to upload: {}", oIpAddress);
			return "error";
		}

		if (Files.exists(pkgRoot))
		try {

			Set<Entry<String, Set<String>>> entrySet = Files.walk(pkgRoot).filter(Files::isRegularFile).map(pkgRoot::relativize)

					.collect(
							Collectors.groupingBy(
									p ->
									p.getParent().toString(),
									Collectors.mapping(
											Path::getFileName,
											Collectors.mapping(
													Path::toString,
													Collectors.toSet()))))
					.entrySet();

			model.addAttribute("pkgs", entrySet);

		} catch (IOException e) {
			logger.catching(e);
		}

		return "hidden/pkg";
	}
}
