package irt.web.bean.jpa;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UserNameRepository extends CrudRepository<UserName, Long> {

	Optional<UserName> findByName(String name);
}
