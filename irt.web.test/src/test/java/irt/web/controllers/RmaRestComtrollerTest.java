package irt.web.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import irt.web.bean.jpa.Rma.Status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class RmaRestComtrollerTest {
	private final static Logger logger = LogManager.getLogger();

	@Autowired private MockMvc mockMvc;

	@Test
	void idsByStatusTest() throws Exception {

		final MvcResult andReturn = mockMvc.perform(
				get("/rest/rma/ids-by-status")
				.param("status", Status.FIXED.name(), Status.WAITTING.name())).
				andDo(print())
				.andExpect(status().isOk())
				.andReturn();

		logger.error(andReturn.getResponse().getContentAsString());
	}

}
