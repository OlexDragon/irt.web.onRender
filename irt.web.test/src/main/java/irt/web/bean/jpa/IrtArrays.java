package irt.web.bean.jpa;

import java.util.List;

import irt.web.service.JSonListConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "arrays")
@NoArgsConstructor @RequiredArgsConstructor @Getter @Setter @ToString
public class IrtArrays{

	@EmbeddedId
	private ArraysId arrayId;

	@NonNull
	@Convert(converter = JSonListConverter.class)
	private List<String> content;
}
