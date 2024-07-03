package irt.web.controllers.hidden;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.web.bean.TrustStatus;
import irt.web.bean.jpa.IpAddress;
import irt.web.bean.jpa.Product;
import irt.web.bean.jpa.ProductRepository;
import irt.web.service.IpService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/hidden")
public class ProductHiddenController implements ErrorController {
	private final Logger logger = LogManager.getLogger();

	@Autowired private ProductRepository	productRepository;
	@Autowired private IpService ipService;

	@GetMapping("products")
	public String products(HttpServletRequest request, Model model) {
		final String remoteAddr = request.getRemoteAddr();
		logger.traceEntry("clientIP: '{}'", remoteAddr);

		final Optional<IpAddress> oRemoteAddress =ipService.getIpAddress(remoteAddr);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			logger.info("{} redirected to error page", oRemoteAddress);
			return "error";
		}

		final Iterable<Product> products = productRepository.findAll();
		model.addAttribute("products", products);

		return "hidden/products";
	}

	@GetMapping("product/{productId}")
	public String product(@PathVariable Long productId, HttpServletRequest request, Model model) {
		final String remoteAddr = request.getRemoteAddr();

		final Optional<IpAddress> oRemoteAddress =ipService.getIpAddress(remoteAddr);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			logger.info("{} redirected to error page", oRemoteAddress);
			return "error";
		}

		productRepository.findById(productId)
		.ifPresent(p->model.addAttribute("product", p));

		return "hidden/product";
	}
}
