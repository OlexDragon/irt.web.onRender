package irt.web.bean.jpa;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
