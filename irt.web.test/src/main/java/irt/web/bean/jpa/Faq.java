package irt.web.bean.jpa;

import jakarta.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity
@NoArgsConstructor @RequiredArgsConstructor @Getter @Setter @ToString
public class Faq{

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long	id;

	@NonNull
	@Accessors(chain = true)
	@Column(nullable = false, unique = true)
	private String 	question;

	@JsonIgnore
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "faqId", referencedColumnName = "id")
	FaqAnswer answer;
}
