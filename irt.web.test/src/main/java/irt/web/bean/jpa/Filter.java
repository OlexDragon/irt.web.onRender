package irt.web.bean.jpa;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table
@Getter @Setter @ToString(exclude = {"mainFilter", "productFilters"})
public class Filter implements Serializable{
	private static final long serialVersionUID = 6315117908794414784L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long	id;
	private Long 	ownerId;
	private String 	name;
	private String 	description;
	@Column(insertable = false)
	private int 	filterOrder;
	@Column(insertable = false)
	private boolean radio;
	@Column(insertable = false)
	private boolean active;

	@Transient
	private boolean selected;

	@ManyToOne(fetch = FetchType.LAZY, optional=true)
    @JoinColumn(name = "ownerId", referencedColumnName ="id", insertable = false, updatable = false)
    private Filter mainFilter;

    @OneToMany(mappedBy="mainFilter", fetch = FetchType.LAZY)
	@OrderBy("filterOrder")
	private List<Filter> children;

    @JsonIgnore
    @OneToMany(mappedBy="filter", fetch = FetchType.LAZY)
	private List<ProductFilter> productFilters;
}
