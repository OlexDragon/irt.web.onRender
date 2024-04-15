package irt.web.hidden;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.web.bean.AboutPage;
import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.IpAddress;
import irt.web.bean.jpa.WebContent;
import irt.web.bean.jpa.WebContentRepository;
import irt.web.service.IpService;

@Controller
@RequestMapping("/hidden")
public class AboutHiddenController implements ErrorController {
	private final Logger logger = LogManager.getLogger();

	@Autowired private WebContentRepository	webRepository;
	@Autowired private IpService ipService;

	@GetMapping("about")
	public String products(@CookieValue(required = false) String clientIP, Model model) {
		logger.traceEntry("clientIP: '{}'", clientIP);

		final Optional<IpAddress> oRemoteAddress = ipService.getIpAddress(clientIP);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			model.addAttribute("errorCode", clientIP);
			logger.info("{} redirected to error page", oRemoteAddress);
			return "error";
		}

		final Iterable<WebContent> aboutContent = webRepository.findByPageName("about");
		AboutPage aboutPage = new AboutPage(aboutContent);
		model.addAttribute("aboutPage", aboutPage);

		return "hidden/about";
	}
}
