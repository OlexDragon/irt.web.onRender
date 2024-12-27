package irt.web.controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import irt.web.bean.jpa.ArraysId;
import irt.web.bean.jpa.ArraysRepository;
import irt.web.bean.jpa.IrtArrays;

@RestController
@RequestMapping("rest/quote")
public class QuoteRestComtroller {
	private final Logger logger = LogManager.getLogger();

	@Autowired private ArraysRepository arraysRepository;

	/**
	 * @param band - selected Band (ex. C, Ku, ...)
	 * @param type - unit type (ex. RACK_MOUNT, OUTDOOR)
	 * @return product names with sizes
	 */
	@PostMapping("product-name")
    List<List<IrtArrays>> productName(@RequestParam String band, String type){
		logger.traceEntry("band: {}; type: {};", band, type);

//		final List<IrtArrays> lArrays = Optional.ofNullable(band).map(b->new ArraysId("band", type, b)).flatMap(arraysRepository::findById).map(o->new IrtArrays[] {o}).map(Arrays::asList).orElseGet(()->arraysRepository.findByArrayIdNameAndArrayIdSubtype("band", type));
//		logger.error(lArrays);

		ArraysId id = new ArraysId("band", type, band);

		// Get all related bands
		List<List<IrtArrays>> list = arraysRepository.findById(id)

				.map(IrtArrays::getContent)
				.map(List::parallelStream)
				// or use only one band
				.orElseGet(()->java.util.Arrays.stream(new String[] { band }))
				// find all product names with given bands
				.map(
						b->{
							final String name = "buc_size_" + type;
							logger.debug("name: {}; subtype: {}'", name, b);
							return arraysRepository.findByArrayIdNameAndArrayIdSubtype(name, b);
						})
				.collect(Collectors.toList());

		logger.debug(list);

		return list;
    }

	@PostMapping("name-exists")
    HashMap<String, Boolean> nameExists(@RequestBody Map<String, String> idName){
		logger.traceEntry("{}", idName);

		HashMap<String, Boolean> map = new HashMap<>();
		idName.entrySet().forEach(e->map.put(e.getKey(), arraysRepository.existsByArrayIdName(e.getValue())));

		logger.error(map);

		return map;
    }

	@PostMapping("supply")
    List<IrtArrays> supply(@RequestParam String productName, String power){
		logger.traceEntry("productName: {}; power:{}", productName, power);

		final List<IrtArrays> arrays = Optional.ofNullable(power)

				.map(p->new ArraysId("supply", productName, p))
				.flatMap(arraysRepository::findById)
				.map(o->new IrtArrays[] {o})
				.map(Arrays::asList)
				.orElseGet(()->arraysRepository.findByArrayIdNameAndArrayIdType("supply", productName));
		logger.error("productName: {}; power:{}", productName, power);

		logger.traceEntry("{}", arrays);

		return arrays;
    }
//
//	private BiFunction<HashMap<String, Set<String>>, ? super IrtArrays, HashMap<String, Set<String>>> accumulator() {
//		return (map, a)->{
//			final Set<String> set = map.get(a.getArrayId().getType());
//
//			if(set==null) 
//				map.put(a.getArrayId().getType(), new HashSet<>(a.getContent()));
//
//			else
//				set.addAll(a.getContent());
//			return map;
//		};
//	}
}
