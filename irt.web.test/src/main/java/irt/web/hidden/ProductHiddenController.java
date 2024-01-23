package irt.web.hidden;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.web.bean.jpa.Product;
import irt.web.bean.jpa.ProductRepository;
import irt.web.bean.jpa.RemoteAddress;
import irt.web.bean.jpa.RemoteAddress.TrustStatus;
import irt.web.bean.jpa.RemoteAddressRepository;

@Controller
@RequestMapping("/hidden")
public class ProductHiddenController implements ErrorController {
	private final Logger logger = LogManager.getLogger();

	private String home = System.getProperty("user.home");

	@Autowired private ProductRepository	productRepository;
	@Autowired private RemoteAddressRepository	 remoteAddressRepository;

	@GetMapping("products")
	public String products(@CookieValue(required = false) String clientIP, Model model) {

		final Optional<RemoteAddress> oRemoteAddress = Optional.ofNullable(clientIP).map(this::getRemoteAddress);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			model.addAttribute("errorCode", clientIP);
			return "error";
		}

		final Iterable<Product> products = productRepository.findAll();
		model.addAttribute("products", products);

		return "hidden/products";
	}

	@GetMapping("product/{productId}")
	public String product(@PathVariable Long productId, @CookieValue String clientIP, Model model) {

		final Optional<RemoteAddress> oRemoteAddress = Optional.ofNullable(clientIP).map(this::getRemoteAddress);

		if(!oRemoteAddress.isPresent() || oRemoteAddress.get().getTrustStatus()!=TrustStatus.IRT) {
			model.addAttribute("errorCode", clientIP);
			return "error";
		}

		productRepository.findById(productId)
		.ifPresent(p->model.addAttribute("product", p));

		return "hidden/product";
	}

	private RemoteAddress getRemoteAddress(final String remoteAddr) {

		return remoteAddressRepository.findById(remoteAddr)

				.map(
						ra->{
							final LocalDateTime now = LocalDateTime.now();
							ra.setConnectionCount(ra.getConnectionCount()+1);
							ra.setLastConnection(now);
							return remoteAddressRepository.save(ra);
						})
				.orElseGet(
						()->{
							final LocalDateTime now = LocalDateTime.now();
							return remoteAddressRepository.save(new RemoteAddress(remoteAddr, now, now, 1, TrustStatus.UNKNOWN));
						});
	}
}
