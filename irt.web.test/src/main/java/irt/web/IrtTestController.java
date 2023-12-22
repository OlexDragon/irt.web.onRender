package irt.web;

import java.net.UnknownHostException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class IrtTestController {

	@GetMapping
    String get() throws UnknownHostException {
		return "home";
    }
}
