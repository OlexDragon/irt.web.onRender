package irt.web.bean.jpa;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface FaqAnswerRepository extends CrudRepository<FaqAnswer, Long> {

	Optional<FaqAnswer> findByFaqId(long faqId);
}
