package irt.web.bean.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity
@NoArgsConstructor @RequiredArgsConstructor @Getter @Setter @ToString
public class FaqAnswer{

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NonNull
	@Column(nullable = false, unique = true)
	private Long faqId;

	@NonNull
	@Accessors(chain = true)
	@Column(nullable = false, length = 1000)
	private String 	answer;
}
