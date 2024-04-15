package irt.web.bean.jpa;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface PartNumberRepository extends CrudRepository<PartNumber, Long> {

	Optional<PartNumber> findByPartNumberIgnoreCase(String partNumber);
}
