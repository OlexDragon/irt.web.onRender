package irt.web.controllers;

import static irt.web.controllers.OnRenderController.getActiveMenuItems;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import irt.web.bean.ProductMenu;
import irt.web.bean.jpa.IrtArrays;
import irt.web.bean.jpa.ArraysRepository;
import irt.web.bean.jpa.WebMenuRepository;

@Controller
@RequestMapping("quote")
public class QuoteComtroller {
	private final Logger logger = LogManager.getLogger();

	@Autowired private WebMenuRepository	menuRepository;
	@Autowired private ArraysRepository 	arraysRepository;


	@ModelAttribute("menus")
	public List<ProductMenu> messages() {
		return getActiveMenuItems(menuRepository);
	}

	@GetMapping
    String quote(Model model){
		return "quote";
    }

	/**
	 * 
	 * @param name - DB 'arrays' name field ex. "C_RACK_MOUNT" or "C_OUTDOOR" ...
	 * @return Subbends from DB
	 */
	@PostMapping("modal")
    String modal(@RequestParam String name, Model model){
		logger.traceEntry("name: {};", name);

		final List<IrtArrays> listArrays = arraysRepository.findByArrayIdNameOrderByArrayIdType(name);
		model.addAttribute("arrayList", listArrays);

		final String[] split = name.split("_", 2);
		final String header = split[0] + " Band : Select Subband";
		model.addAttribute("header", header);

		final boolean isConverter = split[1].equals("CONVERTER");
		model.addAttribute("isConverter", isConverter);

		return "quote :: band";
    }

	/**
	 * 
	 * @param subtype
	 * @return 'powers : size' for given subtype
	 */
	@PostMapping("modal/name")
    String modalName(@RequestParam String subtype, Model model){
		logger.traceEntry("subtype: {};", subtype);

		final List<IrtArrays> listArrays = arraysRepository.findByArrayIdNameAndArrayIdSubtype("power_size", subtype);
		listArrays.sort((a,b)->Double.compare(Double.parseDouble(a.getArrayId().getType()), Double.parseDouble(b.getArrayId().getType())));

		model.addAttribute("name", subtype);
		model.addAttribute("listArrays", listArrays);

		return "quote :: product-power";
    }
}
