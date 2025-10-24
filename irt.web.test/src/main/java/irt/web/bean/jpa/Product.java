package irt.web.bean.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter @ToString
public class Product{

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long	id;
	private String 	name;
	private String 	nameFr;
	@Column(insertable = false, columnDefinition = "default true")
	private boolean active;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "productId")
	List<ProductFilter> productFilters;

	public List<Long> getFilterIds(){
		return Optional.ofNullable(productFilters).map(List::stream).map(s->s.map(mf->mf.getFilterId()).collect(Collectors.toList())).orElse(new ArrayList<>());
	}
}
