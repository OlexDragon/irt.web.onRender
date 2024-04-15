package irt.web.bean.email;

import irt.web.controllers.OnRenderRestController.ResponseMessage;

public interface MailWorker {

	ResponseMessage sendEmail(WebEmail webEmail);

}
