package irt.web.bean.jpa;

import jakarta.persistence.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor @RequiredArgsConstructor @Getter @Setter @ToString
public class PartNumber{

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long	id;

	@NonNull
	@Column(nullable = false, unique = true)
	private String 	partNumber;
	@NonNull
	@Column(nullable = false, unique = false)
	private String 	description;
}
