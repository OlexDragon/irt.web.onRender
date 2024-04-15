package irt.web.bean.jpa;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface IpAddressRepository extends CrudRepository<IpAddress, Long> {

	Optional<IpAddress> findByAddress(String ip);
}
