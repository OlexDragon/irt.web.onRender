package irt.web.bean;

import java.util.List;
import java.util.Optional;

import irt.web.bean.jpa.Filter;
import irt.web.bean.jpa.WebMenu;
import irt.web.bean.jpa.WebMenuFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class ProductSubmenu {

	private final Long id;
	private final String name;
	private final String nameFr;
	private final Long filterId;
	private final Boolean active;
	private final boolean radio;

	public ProductSubmenu(WebMenu webMenu) {

		id = webMenu.getId();
		name = webMenu.getName();
		active = webMenu.getActive();
		nameFr = webMenu.getNameFr();

		final Optional<Filter> oFilter = Optional.ofNullable(webMenu.getMenuFilters()).map(List::stream).flatMap(s->s.map(WebMenuFilter::getFilter).findAny());
		if(oFilter.isPresent()) {
			final Filter filter = oFilter.get();
			filterId = filter.getId();
			radio = filter.isRadio();

		}else {
			filterId = null;
			radio = false;
		}
	}
}
