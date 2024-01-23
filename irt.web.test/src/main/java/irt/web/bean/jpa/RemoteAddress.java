package irt.web.bean.jpa;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table
@NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString
public class RemoteAddress implements Serializable{
	private static final long serialVersionUID = 6315117908794414784L;

	@Id
	private String 	address;
	private LocalDateTime 	firstConnection;
	private LocalDateTime 	lastConnection;
	private int 	connectionCount;
	@Enumerated(EnumType.ORDINAL)
	private TrustStatus trustStatus;

	public enum TrustStatus{
		UNKNOWN,
		TRUSTED,
		NOT_TRUSTED,
		IRT
	}
}
