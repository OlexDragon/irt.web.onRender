package irt.web.bean.email;

import java.io.IOException;

import irt.web.controllers.OnRenderRestController.ResponseMessage;

public interface MailWorker {

	ResponseMessage sendEmail(WebEmail webEmail) throws IOException;

}
