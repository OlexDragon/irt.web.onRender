package irt.web.bean.jpa;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Id;

import irt.web.bean.ConnectTo;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table
@NoArgsConstructor @RequiredArgsConstructor @Getter @Setter @ToString
public class IpConnection implements Serializable{
	private static final long serialVersionUID = 6315117908794414784L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NonNull
	private Long ipId;
	@NonNull
	private LocalDateTime 	date;
	@NonNull
	@Enumerated(EnumType.ORDINAL)
	private ConnectTo connectTo;
}
