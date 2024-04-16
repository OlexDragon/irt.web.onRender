package irt.web.controllers;

import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import irt.web.bean.ProductMenu;
import irt.web.bean.ProductSubmenu;
import irt.web.bean.SliderCard;
import irt.web.bean.jpa.Filter;
import irt.web.bean.jpa.FilterRepository;
import irt.web.bean.jpa.Product;
import irt.web.bean.jpa.ProductFilter;
import irt.web.bean.jpa.WebContent;
import irt.web.bean.jpa.WebContentRepository;
import irt.web.bean.jpa.WebMenuRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/")
public class OnRenderController implements ErrorController {
	private final static Logger logger = LogManager.getLogger();

	@Value("${irt.web.product.par_page}")
	private Integer productParPage;

	@Autowired private EntityManager		entityManager;

	@Autowired private WebMenuRepository	menuRepository;
	@Autowired private FilterRepository		filterRepository;
	@Autowired private WebContentRepository	webRepository;

	@ModelAttribute("menus")
	public List<ProductMenu> menus() {
		return getActiveMenuItems(menuRepository);
	}

	@GetMapping
	public String home(Model model) throws UnknownHostException {

		final List<WebContent> sliderCardFields = webRepository.findByPageName("home_slider");
		final Map<Integer, SliderCard> fieldsToCards = SliderCard.fieldsToCards(sliderCardFields);
		model.addAttribute("cards", fieldsToCards);

		return "home";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

		final String attribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE).toString();
		final Integer code = Integer.valueOf(attribute);
		final String errorCode = Optional.ofNullable(HttpStatus.resolve(code)).map(HttpStatus::getReasonPhrase).map(f->attribute + " - " + f).orElse(attribute);
		model.addAttribute("errorCode", errorCode);

		return "error";
    }

	@GetMapping("about")
	public String about(Model model) throws UnknownHostException {
		return "about";
    }

	@GetMapping("products")
	public String products(
			@RequestParam(name = "filter", required = false) List<Long> selected, 
			@RequestParam(required = false) String search,  
			Model model) throws UnknownHostException {

		final int page = 0;
		logger.traceEntry("selected filters: {}; search: {};", selected, search);

		model.addAttribute("search", search);
		model.addAttribute("pageSize", productParPage);

		final List<AbstractMap.SimpleEntry <String, Filter>> selectedFilters = new ArrayList<>();
		final List<Filter> filters = filterRepository.findByOwnerIdIsNullAndActiveOrderByFilterOrderAsc(true);

		// Check Selected Filters
		Optional.ofNullable(selected)
		.ifPresent(
				s->{
					s.forEach(
							filterId->{
								filters.forEach(
										mainFilter->{
											Optional.ofNullable(mainFilter.getChildren())
											.ifPresent(
													fs->{
														for(int i=page; i<fs.size(); i++) {
															final Filter f = fs.get(i);
															if(f.getId()==filterId){
																f.setSelected(true);
																selectedFilters.add(new AbstractMap.SimpleEntry <>(mainFilter.getName(), f));
																break;
															}
														}
													});
										});
							});
				});
		model.addAttribute("filters", filters);
		model.addAttribute("selectedFilters", selectedFilters);
		modelAddProducts(model, search, selected, page);

		return "products";
    }

	@GetMapping("products/search")
	public String searchProduct(@RequestParam(required = false) List<Long> filter, 
						@RequestParam(required = false) String search, 
						@RequestParam(required = false) Integer page, 
						Model model) throws InterruptedException {

		logger.traceEntry("filter: {}; search: {}; page: {};", filter, search, page);

		modelAddProducts(model, search, filter, page);

		return "products :: products_content";
	}

	@GetMapping("news-events")
	public String events(Model model) throws InterruptedException {
		logger.traceEntry();

		return "news-events";
	}

	private void modelAddProducts(Model model, String search, List<Long> filterIDs, Integer page) {
		logger.traceEntry("search: {}; filterIDs: {}; page: {};", search, filterIDs, page);
	
		final List<Product> products = getProducts(search, filterIDs, page);
		model.addAttribute("products", products);
	}

	private List<Product> getProducts(String search, List<Long> filterIDs, Integer page) {

		final CriteriaBuilder criteriaBuilder		 = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Product> criteriaQuery	 = criteriaBuilder.createQuery(Product.class);

		final Root<ProductFilter> productFilterRoot = criteriaQuery.from(ProductFilter.class);

		// JOIN Product
		final Join<ProductFilter, Product> filterJoin = productFilterRoot.join("product");

		//  WHERE filter ID equals
		final AtomicReference<Predicate> where = new AtomicReference<>();
		final Optional<List<Long>> oFilters = Optional.ofNullable(filterIDs).filter(ids->!ids.isEmpty());
		oFilters.ifPresent(
				ids->{
					final List<Predicate> predicates = new ArrayList<>();
					filterIDs.forEach(id -> predicates.add(criteriaBuilder.equal(productFilterRoot.get("filterId"), id)));
					final Predicate[] array = predicates.toArray(new Predicate[predicates.size()]);
					where.set(criteriaBuilder.or(array));
				});

		//  WHERE search LIKE
		Optional.ofNullable(search).filter(s->!s.isEmpty())
		.ifPresent(
				s->{
					final Predicate like = criteriaBuilder.like(criteriaBuilder.lower(filterJoin.get("name")), '%' + search.toLowerCase() + '%');
					final Predicate predicate = where.get();

					if(predicate==null)
						where.set(like);

					else
						where.set(criteriaBuilder.and(like, predicate));
				});

		final Predicate predicate = where.get();

		if(predicate!=null)
			criteriaQuery.where(predicate);

		final CriteriaQuery<Product> groupBy = criteriaQuery.groupBy(filterJoin.get("id"));
		oFilters.ifPresent(
				ids->{
					final Predicate greaterThanOrEqualTo = criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.count(productFilterRoot.get("filterId")), (long)ids.size());
					groupBy.having(greaterThanOrEqualTo);
				});



		
		final CriteriaQuery<Product> select = criteriaQuery.select(productFilterRoot.get("product")).distinct(true);
		final TypedQuery<Product> query = entityManager.createQuery(select);
		final int startPosition = page * productParPage;
		query.setFirstResult(startPosition);
		query.setMaxResults(startPosition + productParPage);

		return query.getResultList();
	}

	public static List<ProductMenu> getActiveMenuItems( WebMenuRepository	repository) {
		List<ProductMenu> menus = new ArrayList<>();
		repository.findByActiveOrderByMenuOrderAsc(true)
		.forEach(
				m->{

					if(m.getOwnerId()==null){
						Optional<ProductMenu> oPMenu = menus.parallelStream().filter(pm->pm.getId()==m.getId()).findAny();
						if(oPMenu.isPresent()) {
							final ProductMenu pm = oPMenu.get();
							pm.setName(m.getName());
							pm.setOrder(m.getMenuOrder());
							pm.setActive(m.getActive());
						}

						else
							menus.add(new ProductMenu(m));

						return;
					}

					Optional<ProductMenu> oPMenu = menus.parallelStream().filter(pm->pm.getId()==m.getOwnerId()).findAny();
					if(oPMenu.isPresent()) 
						oPMenu.get().getSubmenus().add(new ProductSubmenu(m));

					else {
						final ProductMenu productMenu = new ProductMenu();
						productMenu.setId(m.getOwnerId());
						productMenu.getSubmenus().add(new ProductSubmenu(m));
						menus.add(productMenu);
					}
				});

		return menus.stream().filter(pm->pm.getActive()!=null).sorted((a,b)->a.getOrder()-b.getOrder()).collect(Collectors.toList());
	}
}
