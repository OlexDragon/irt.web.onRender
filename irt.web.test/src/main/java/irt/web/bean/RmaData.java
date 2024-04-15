package irt.web.bean;

import java.time.LocalDateTime;

import irt.web.bean.jpa.Rma;
import irt.web.bean.jpa.Rma.Status;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RmaData {

	private Long	id;
	private String 	rmaNumber;
	private String 	malfunction;
	private String 	serialNumber;
	private String 	partNumber;
	private String 	description;
	private String 	fullName;
	private LocalDateTime creationDate;
	private Status	status;

	private final String username = "WEB";
	private final boolean fromWeb = true;

	public RmaData(Rma rma) {
		id			 = rma.getId();
		rmaNumber	 = rma.getRmaNumber();
		malfunction	 = rma.getMalfunction();
		serialNumber = rma.getSerialNumber().getSerialNumber();
		partNumber	 = rma.getSerialNumber().getPartNumber().getPartNumber();
		description	 = rma.getSerialNumber().getPartNumber().getDescription();
		fullName	 = rma.getUserName().getName();
		creationDate = rma.getCreationDate().getDate();
		status 		 = rma.getStatus();
	}
}
