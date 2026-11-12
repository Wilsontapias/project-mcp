package com.tdea.proyect_mcp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.ai.mcp.server.enabled=false")
class ProyectMcpApplicationTests {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void connectsToH2Database() {
		Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

		assertEquals(1, result);
	}

}
