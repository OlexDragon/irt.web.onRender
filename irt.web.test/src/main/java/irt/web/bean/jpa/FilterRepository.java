package irt.web.bean.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface FilterRepository extends CrudRepository<Filter, Long> {

	List<Filter> findAllByOrderByFilterOrderAsc						();
	List<Filter> findByActiveOrderByFilterOrderAsc					(boolean active);
	List<Filter> findByOwnerIdIsNullOrderByFilterOrderAsc			();
	List<Filter> findByOwnerIdOrderByFilterOrderAsc					(Long ownerId);
	List<Filter> findByOwnerIdIsNullAndActiveOrderByFilterOrderAsc	(boolean active);
	List<Filter> findByOwnerIdAndActiveOrderByFilterOrderAsc		(Long ownerId, boolean active);
}
