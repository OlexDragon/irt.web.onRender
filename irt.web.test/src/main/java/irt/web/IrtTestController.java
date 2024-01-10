package irt.web;

import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import irt.web.bean.jpa.Filter;
import irt.web.bean.jpa.FilterRepository;
import irt.web.bean.jpa.Product;
import irt.web.bean.jpa.ProductFilter;
import irt.web.bean.jpa.ProductRepository;
import irt.web.bean.jpa.WebMenu;
import irt.web.bean.jpa.WebMenuRepository;

@Controller
@RequestMapping("/")
public class IrtTestController implements ErrorController {
	private final Logger logger = LogManager.getLogger();

	@Value("${irt.web.product.par_page}")
	private Integer productParPage;

	@Autowired private EntityManager		entityManager;

	@Autowired private WebMenuRepository	menuRepository;
	@Autowired private FilterRepository		filterRepository;
	@Autowired private ProductRepository	productRepository;

	@GetMapping
    String get(Model model) throws UnknownHostException {
		final List<WebMenu> menus = menuRepository.findByOwnerIdIsNullAndActiveOrderByMenuOrderAsc(true);
		model.addAttribute("menus", menus);
		return "home";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

    	final List<WebMenu> menus = menuRepository.findByOwnerIdIsNullAndActiveOrderByMenuOrderAsc(true);
		model.addAttribute("menus", menus);

		final String attribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE).toString();
		final Integer code = Integer.valueOf(attribute);
		final String errorCode = Optional.ofNullable(HttpStatus.resolve(code)).map(HttpStatus::getReasonPhrase).map(f->attribute + " - " + f).orElse(attribute);
		model.addAttribute("errorCode", errorCode);

		return "error";
    }

	@GetMapping("about")
    String about(Model model) throws UnknownHostException {
		final List<WebMenu> menus = menuRepository.findByOwnerIdIsNullAndActiveOrderByMenuOrderAsc(true);
		model.addAttribute("menus", menus);
		return "about";
    }

	@GetMapping("products")
    String products(@RequestParam(name = "filter", required = false) List<Long> selected, 
    				@RequestParam(required = false) String search,  
    				Model model) throws UnknownHostException {

		final int page = 0;
		logger.traceEntry("selected filters: {}; search: {};", selected, search);

		model.addAttribute("search", search);
		model.addAttribute("pageSize", productParPage);
		final List<WebMenu> menus = menuRepository.findByOwnerIdIsNullAndActiveOrderByMenuOrderAsc(true);
		model.addAttribute("menus", menus);

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
											Optional.ofNullable(mainFilter.getSubFilters())
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
	String searchProduct(@RequestParam(required = false) List<Long> filter, 
						@RequestParam(required = false) String search, 
						@RequestParam(required = false) Integer page, 
						Model model) throws InterruptedException {

		logger.traceEntry("filter: {}; search: {}; page: {};", filter, search, page);

		modelAddProducts(model, search, filter, page);

		return "products :: products_content";
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

		//  WHERE filter ID equals
		final Optional<List<Long>> oFilters = Optional.ofNullable(filterIDs).filter(ids->!ids.isEmpty());
		oFilters.ifPresent(
				ids->{
					final List<Predicate> predicates = new ArrayList<>();
					filterIDs.forEach(id -> predicates.add(criteriaBuilder.equal(productFilterRoot.get("filterId"), id)));
					final Predicate[] array = predicates.toArray(new Predicate[predicates.size()]);
					Predicate where = criteriaBuilder.or(array);
					criteriaQuery.where(where);
				});

		// JOIN Product
		final Join<ProductFilter, Product> filterJoin = productFilterRoot.join("product");

		//  WHERE search LIKE
		Optional.ofNullable(search).filter(s->!s.isEmpty())
		.ifPresent(
				s->{
					final Predicate likePredicate = criteriaBuilder.like(filterJoin.get("name"), '%' + search + "%");
					criteriaQuery.where(likePredicate);
				});

		final CriteriaQuery<Product> groupBy = criteriaQuery.groupBy(filterJoin.get("id"));
		oFilters.ifPresent(
				ids->{
					final Predicate greaterThanOrEqualTo = criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.count(productFilterRoot.get("filterId")), (long)ids.size());
					groupBy.having(greaterThanOrEqualTo);
				});



		
		final CriteriaQuery<Product> select = criteriaQuery.select(productFilterRoot.get("product"));
		final TypedQuery<Product> query = entityManager.createQuery(select);
		final int startPosition = page * productParPage;
		query.setFirstResult(startPosition);
		query.setMaxResults(startPosition + productParPage);

		return query.getResultList();
	}
}
