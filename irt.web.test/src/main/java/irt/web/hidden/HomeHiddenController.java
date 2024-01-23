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
@RequestMapping("hidden/home")
public class HomeHiddenController implements ErrorController {
	private final Logger logger = LogManager.getLogger();

	private String home = System.getProperty("user.home");

	@Autowired private ProductRepository	productRepository;
	@Autowired private RemoteAddressRepository	 remoteAddressRepository;

	@GetMapping
	public String products(@CookieValue(required = false) String clientIP, Model model) {

		return "hidden/home";
	}
}
