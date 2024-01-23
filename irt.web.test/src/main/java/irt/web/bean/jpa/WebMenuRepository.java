package irt.web.bean.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface WebMenuRepository extends CrudRepository<WebMenu, Long> {

	List<WebMenu> findAllByOrderByMenuOrderAsc();
	List<WebMenu> findByActiveOrderByMenuOrderAsc(boolean active);
	List<WebMenu> findByOwnerIdIsNullOrderByMenuOrderAsc();
	List<WebMenu> findByOwnerIdOrderByMenuOrderAsc(Long ownerId);
	List<WebMenu> findByOwnerIdIsNullAndActiveOrderByMenuOrderAsc(boolean active);
	List<WebMenu> findByOwnerIdAndActiveOrderByMenuOrderAsc(Long ownerId, boolean active);
}
