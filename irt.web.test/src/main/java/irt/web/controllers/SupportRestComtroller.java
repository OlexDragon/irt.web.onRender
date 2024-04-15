package irt.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import irt.web.bean.jpa.FaqAnswer;
import irt.web.bean.jpa.FaqAnswerRepository; 

@RestController
@RequestMapping("rest/suport")
public class SupportRestComtroller {
	@Autowired private FaqAnswerRepository	 answerRepository;

	@PostMapping("answer")
    String answer(@RequestParam Long faqID){
		return answerRepository.findByFaqId(faqID).map(FaqAnswer::getAnswer).orElse("");
	}
}
