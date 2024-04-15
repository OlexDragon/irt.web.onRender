package irt.web.hidden;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.web.bean.SliderCard;
import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.IpAddress;
import irt.web.bean.jpa.WebContent;
import irt.web.bean.jpa.WebContentRepository;
import irt.web.service.IpService;

@Controller
@RequestMapping("hidden")
public class HomeHiddenController implements ErrorController {
	private final static Logger logger = LogManager.getLogger();

	@Value("${irt.web.root.path}")
	private String root;

	@Autowired private WebContentRepository	webRepository;
	@Autowired private IpService ipService;

	@GetMapping
	public String products(@CookieValue(required = false) String clientIP, Model model) {

		final Optional<IpAddress> oRemoteAddress = ipService.getIpAddress(clientIP);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			model.addAttribute("errorCode", clientIP);
			logger.info("{} redirected to error page", oRemoteAddress);
			return "error";
		}

		final List<WebContent> sliderCardFields = webRepository.findByPageName("home_slider");
		final Map<Integer, SliderCard> fieldsToCards = SliderCard.fieldsToCards(sliderCardFields);
		model.addAttribute("cards", fieldsToCards);

		return "hidden/home";
	}
}
