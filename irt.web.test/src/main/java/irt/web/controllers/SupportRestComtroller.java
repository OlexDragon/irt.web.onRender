package irt.web.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import irt.web.bean.jpa.FaqAnswerRepository; 

@RestController
@RequestMapping("rest/suport")
public class SupportRestComtroller {

	@Autowired private FaqAnswerRepository	 answerRepository;

	@PostMapping("answer")
    String answer(@CookieValue(required = false) String localeInfo, @RequestParam Long faqID){
		return answerRepository.findByFaqId(faqID).map(fa->Optional.ofNullable(localeInfo).filter(l->l.startsWith("fr")).map(_->fa.getAnswerFr()).orElse(fa.getAnswer())).orElse("");
	}
}
