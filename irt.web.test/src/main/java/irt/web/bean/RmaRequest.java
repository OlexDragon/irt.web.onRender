package irt.web.bean;

import org.apache.commons.validator.routines.EmailValidator;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class RmaRequest {

	private String name;
	private String email;
	private String sn;
	private String cause;

	public boolean isValid() {

		final boolean isName	 = name!=null && !name.trim().isEmpty();
		final boolean isEmail	 = EmailValidator.getInstance().isValid(email);
		final boolean isSN		 = sn!=null && !sn.trim().isEmpty();
		final boolean isCause	 = cause!=null && !cause.trim().isEmpty() && cause.length()<=1000;

		return isName && isEmail && isSN && isCause;
	}
}
