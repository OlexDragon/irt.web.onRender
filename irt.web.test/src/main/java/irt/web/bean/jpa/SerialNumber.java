package irt.web.bean.jpa;

import jakarta.persistence.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
