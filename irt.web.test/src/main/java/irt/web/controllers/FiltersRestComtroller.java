package irt.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import irt.web.bean.jpa.Filter;
import irt.web.bean.jpa.FilterRepository;
import irt.web.bean.jpa.ProductFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@RestController
@RequestMapping("rest/filter")
public class FiltersRestComtroller {
//	private final Logger logger = LogManager.getLogger();

	@Autowired private EntityManager		entityManager;
	@Autowired private FilterRepository		repository;

	@PostMapping("accessible")
	List<Long> getAccessibleProductFilters(@RequestParam(name = "filterIDs[]", required = false) Long... filterIDs) {

		if(filterIDs==null || filterIDs.length==0)
			return StreamSupport.stream(repository.findByActiveOrderByFilterOrderAsc(true).spliterator(), true).map(Filter::getId).collect(Collectors.toList());

		final CriteriaBuilder cb		 = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Long> cq	 = cb.createQuery(Long.class);
		final Root<ProductFilter> root = cq.from(ProductFilter.class);

		final Subquery<Long> subquery = subquery(cb, cq.subquery(Long.class), filterIDs);

		cq.select(root.get("filterId")).where(cb.in(root.get("productId")).value(subquery)).distinct(true);

		return entityManager.createQuery(cq).getResultList();
	}

	private Subquery<Long> subquery(CriteriaBuilder cb, Subquery<Long> sq, Long[] filterIDs) {

		final Root<ProductFilter> root = sq.from(ProductFilter.class);
		final List<Predicate> predicates = new ArrayList<>();

		final Predicate predicate = cb.greaterThanOrEqualTo(cb.count(root.get("productId")), Long.valueOf(filterIDs.length));
		sq.groupBy(root.get("productId")).having(predicate);

		root.alias("productId");

		for(Long l: filterIDs)
			predicates.add(cb.equal(root.get("filterId"), l));

		return sq.select(root.get("productId")).where(cb.or(predicates.toArray(new Predicate[predicates.size()])));
	}
}
