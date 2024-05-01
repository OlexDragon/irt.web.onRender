package irt.web.bean.jpa;

import jakarta.persistence.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor @RequiredArgsConstructor @Getter @Setter @ToString
public class Rma{

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long	id;

	@NonNull
	@Column(nullable = false, unique = true)
	private String 	rmaNumber;

	@NonNull
	@Enumerated(EnumType.ORDINAL)
	private Status	status;

	@NonNull
	@Column(nullable = false, unique = false, length = 1000)
	private String 	malfunction;

	@NonNull
	@Column(nullable = false, unique = false)
	private Long 	serialNumberId;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "serialNumberId", referencedColumnName = "id", insertable = false, updatable = false)
	private SerialNumber serialNumber;

	@NonNull
	@Column(nullable = false, unique = false)
	private Long	 userNameId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userNameId", referencedColumnName = "id", insertable = false, updatable = false)
	private UserName userName;

	@NonNull
	@Column(nullable = false, unique = false)
	private Long	 userEmailId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userEmailId", referencedColumnName = "id", insertable = false, updatable = false)
	private Email userEmail;

	@NonNull
	@Column(nullable = false, unique = false, insertable = true, updatable = false)
	private Long	 creationDateId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creationDateId", referencedColumnName = "id", insertable = false, updatable = false)
	private IpConnection creationDate;

	@NonNull
	@Column(nullable = false, unique = false)
	private Long	 statusCangeDateId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "statusCangeDateId", referencedColumnName = "id", insertable = false, updatable = false)
	private IpConnection statusCangeDate;

	public enum Status{
		IN_WORK,
		SHIPPED,
		READY,
		CREATED,
		CLOSED,
		FIXED,
		WAITTING,
		FINALIZED;
	}
}
