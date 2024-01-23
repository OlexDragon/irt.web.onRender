package irt.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class IrtTestConfig implements WebMvcConfigurer {
//
//	@Autowired private WebContentRepository	 	webContentRepository;
//
//	@Bean
//	public JavaMailSender getJavaMailSender() {
//
//		final List<WebContent> byPageName = webContentRepository.findByPageName("email");
//		final IrtEMailData irtEMailData = new IrtEMailData(byPageName);
//
//		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//	    mailSender.setHost("smtp.office365.com");
//	    mailSender.setPort(587);
//	    
//	    mailSender.setUsername(irtEMailData.getFrom());
//	    mailSender.setPassword(irtEMailData.getPassword());
//	    
//	    Properties props = mailSender.getJavaMailProperties();
//	    props.put("mail.transport.protocol", "smtp");
//	    props.put("mail.smtp.ssl.trust", "smtp.office365.com");
//	    props.put("mail.smtp.auth", "true");
//	    props.put("mail.smtp.starttls.enable", "true");
//	    props.put("mail.smtp.connectiontimeout", 5000);
//	    props.put("mail.smtp.timeout", 3000);
//	    props.put("mail.smtp.writetimeout", 5000);
////	    props.put("mail.debug", "true");
//	    
//	    return mailSender;
//	}
}