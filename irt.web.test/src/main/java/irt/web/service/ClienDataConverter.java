package irt.web.service;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import irt.web.bean.IpData;

public class ClienDataConverter implements Converter<String, Optional<IpData>> {
	private final static Logger logger = LogManager.getLogger();

	private final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Override
	public Optional<IpData> convert(String source) {
		logger.traceEntry(source);

		try {
			return Optional.of(mapper.readValue(source, IpData.class));
		} catch (JsonProcessingException e) {
			logger.catching(e);
		}

		return Optional.empty();
	}

}
