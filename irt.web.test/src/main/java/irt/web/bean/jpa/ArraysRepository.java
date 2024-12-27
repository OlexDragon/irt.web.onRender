package irt.web.bean.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ArraysRepository extends CrudRepository<IrtArrays, ArraysId> {

	List<IrtArrays> findByArrayIdNameOrderByArrayIdType(String name);
	List<IrtArrays> findByArrayIdNameAndArrayIdType(String name, String type);
	List<IrtArrays> findByArrayIdNameAndArrayIdSubtype(String name, String subtype);

	Boolean existsByArrayIdName(String name);

}
