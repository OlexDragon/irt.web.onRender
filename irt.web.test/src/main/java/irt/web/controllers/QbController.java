package irt.web.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class QbController implements ErrorController {

	@GetMapping("qb-eula")
	String qbEula() {
		return "qb/qb-eula";
	}

	@GetMapping("qb-pp")
	String qbPp() {
		return "qb/qb-pp";
	}
}
