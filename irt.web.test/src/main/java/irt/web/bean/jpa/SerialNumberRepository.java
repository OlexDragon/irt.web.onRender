package irt.web.bean.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface SerialNumberRepository extends CrudRepository<SerialNumber, Long> {

	boolean existsBySerialNumberIgnoreCase(String serialNumber);
	Optional<SerialNumber> findBySerialNumber(String serialNumber);
	List<SerialNumber> findBySerialNumberEndingWithIgnoreCase(String serialNumber, Pageable page);
}
