package irt.web.bean.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor @RequiredArgsConstructor @Getter @Setter @ToString
public class SerialNumber{

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long	id;

	@NonNull
	@Column(nullable = false, unique = true)
	private String 	serialNumber;
	@NonNull
	@Column(nullable = true, unique = false)
	private Long	 partNumberId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partNumberId", referencedColumnName = "id", insertable = false, updatable = false)
	private PartNumber partNumber;
}
