package irt.web.bean.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface WebContentRepository extends CrudRepository<WebContent, WebContentId> {

	List<WebContent> findByPageName(String webPage);
}
