package irt.web.bean.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {

	Optional<Product> 	findByPartNumberOrderByPartNumberAsc											( String partNumber											);
	List<Product> 		findByActiveTrueOrderByPartNumberAsc											(										  Pageable pageable	);
	List<Product> 		findByNameContainsAndActiveTrueOrderByPartNumberAsc								(						  String search	, Pageable pageable	);
	List<Product> 		findByProductFiltersFilterIdAndActiveTrueOrderByPartNumberAsc					( Long filterId							, Pageable pageable	);
	List<Product> 		findByProductFiltersFilterIdInAndActiveTrueOrderByPartNumberAsc					( List<Long> filterIds					, Pageable pageable	);
	List<Product> 		findByProductFiltersFilterIdAndNameContainsAndActiveTrueOrderByPartNumberAsc	( Long filterId			, String search	, Pageable pageable	);
	List<Product> 		findByProductFiltersFilterIdInAndNameContainsAndActiveTrueOrderByPartNumberAsc	( List<Long> filterIds	, String search	, Pageable pageable	);
}
