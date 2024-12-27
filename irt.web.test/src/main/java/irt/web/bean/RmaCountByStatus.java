package irt.web.bean;

import irt.web.bean.jpa.Rma.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor @Getter @ToString
public class RmaCountByStatus {

	private Status	 status;
	private long	 count;
}
