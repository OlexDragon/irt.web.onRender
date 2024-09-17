package irt.web.controllers.hidden;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.IpAddress;
import irt.web.service.IpService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/hidden")
public class EventHiddenController implements ErrorController {
	private final Logger logger = LogManager.getLogger();

	@Autowired private IpService ipService;

	@GetMapping("news-events")
	public String products(HttpServletRequest request, Model model) {
		final String remoteAddr = Optional.ofNullable(request.getHeader( "X-Forwarded-For" )).orElseGet(()->request.getRemoteAddr());
		logger.traceEntry("clientIP: '{}'", remoteAddr);

		final Optional<IpAddress> oRemoteAddress = ipService.getIpAddress(remoteAddr);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			logger.info("{} redirected to error page", oRemoteAddress);
			return "error";
		}

		return "hidden/news-events";
	}
}
