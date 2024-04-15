package irt.web.bean.jpa;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {

	List<Product> 		findByActiveTrue											(										  Pageable pageable	);
	List<Product> 		findByNameContainsAndActiveTrue								(						  String search	, Pageable pageable	);
	List<Product> 		findByProductFiltersFilterIdAndActiveTrue					( Long filterId							, Pageable pageable	);
	List<Product> 		findByProductFiltersFilterIdInAndActiveTrue					( List<Long> filterIds					, Pageable pageable	);
	List<Product> 		findByProductFiltersFilterIdAndNameContainsAndActiveTrue	( Long filterId			, String search	, Pageable pageable	);
	List<Product> 		findByProductFiltersFilterIdInAndNameContainsAndActiveTrue	( List<Long> filterIds	, String search	, Pageable pageable	);
}
