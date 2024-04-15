package irt.web.bean.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter @ToString(exclude = {"filters"})
public class Product{

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long	id;
	private String 	name;
	@Column(insertable = false, columnDefinition = "default true")
	private boolean active;

	@OneToMany(targetEntity = ProductFilter.class, mappedBy = "filter", fetch = FetchType.LAZY)
	List<Filter> filters;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "productId")
	List<ProductFilter> productFilters;

	public List<Long> getFilterIds(){
		return Optional.ofNullable(productFilters).map(List::stream).map(s->s.map(mf->mf.getFilterId()).collect(Collectors.toList())).orElse(new ArrayList<>());
	}
}
