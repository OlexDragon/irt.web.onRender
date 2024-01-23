package irt.web.bean.jpa;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table
@Getter @Setter @ToString(exclude = "mainMenu")
public class WebMenu{

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long	 id;
	@Column(nullable = true)
	private Long	 ownerId;
	private String name;
	@Column(insertable = false)
	private String link;
	@Column(insertable = false)
	private Integer menuOrder;
	@Column(insertable = false)
	private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional=true)
    @JoinColumn(name = "ownerId", referencedColumnName ="id", insertable = false, updatable = false)
    private WebMenu mainMenu;
    
    @OneToMany(mappedBy="mainMenu", fetch = FetchType.LAZY)
	@OrderBy("menuOrder")
	private List<WebMenu> submenus;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "menuId")
	List<WebMenuFilter> menuFilters;

	public String getFilterUrl(){

		final Set<Long> ids = Optional.ofNullable(menuFilters).map(List::parallelStream).map(stream->stream.map(WebMenuFilter::getFilterId).collect(Collectors.toSet())).orElse(new HashSet<>());

		Optional.ofNullable(getMainMenu()).map(WebMenu::getMenuFilters).map(List::parallelStream).map(stream->stream.map(WebMenuFilter::getFilterId).collect(Collectors.toSet())).filter(set->!set.isEmpty()).ifPresent(ids::addAll);
		
		return  '?' + ids.stream().map(id->"filter=" + id).collect(Collectors.joining("&"));
	}
}
