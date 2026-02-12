package irt.web.bean;

import java.util.ArrayList;
import java.util.List;

import irt.web.bean.jpa.WebMenu;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor @Getter @Setter @ToString
public class ProductMenu {

	private Long id;
	private String name;
	private String nameFr;
	private Integer order;
	private Boolean active;

	private final List<ProductSubmenu> submenus = new ArrayList<>();

	public ProductMenu(WebMenu webMenu) {
		id = webMenu.getId();
		name = webMenu.getName();
		nameFr= webMenu.getNameFr();
		order = webMenu.getMenuOrder();
		active = webMenu.getActive();
	}
}
