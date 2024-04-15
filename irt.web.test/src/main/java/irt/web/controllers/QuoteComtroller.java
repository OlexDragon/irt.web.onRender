package irt.web.controllers;

import static irt.web.controllers.OnRenderController.getActiveMenuItems;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import irt.web.bean.ProductMenu;
import irt.web.bean.jpa.WebMenuRepository;

@Controller
@RequestMapping("quote")
public class QuoteComtroller {
//	private final Logger logger = LogManager.getLogger();

	private final static int[][] power = {{8, 10, 16}, {20, 25, 30}, {50, 60, 80}, {100, 125, 150}, {200, 250, 300}, {400, 500, 1000}};
	@Autowired private WebMenuRepository	menuRepository;


	@ModelAttribute("menus")
	public List<ProductMenu> messages() {
		return getActiveMenuItems(menuRepository);
	}

	@GetMapping
    String quote(Model model){
		model.addAttribute("power", power);
		return "quote";
    }
}
