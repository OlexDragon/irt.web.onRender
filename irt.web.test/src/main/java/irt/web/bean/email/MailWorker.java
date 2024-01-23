package irt.web.bean.email;

import com.microsoft.graph.models.BodyType;

public interface MailWorker {

	void sendEmail(String from, String subject, String body, BodyType bodyType, String... to);
}
