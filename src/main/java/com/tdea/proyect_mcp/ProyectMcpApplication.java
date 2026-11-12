package com.tdea.proyect_mcp;

import com.tdea.proyect_mcp.tools.BankingMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

/**
 * Main entry point for the local banking MCP server.
 */
@SpringBootApplication
public class ProyectMcpApplication {

    /**
     * Starts the Spring Boot application and its MCP STDIO transport.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(ProyectMcpApplication.class, args);
    }

    /**
     * Registers the annotated banking methods as MCP tools.
     *
     * @param bankingMcpTools tool facade exposed to MCP clients
     * @return provider containing the banking tool callbacks
     */
    @Bean
    @ConditionalOnBean(BankingMcpTools.class)
    public ToolCallbackProvider bankingTools(BankingMcpTools bankingMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(bankingMcpTools)
                .build();
    }
}
