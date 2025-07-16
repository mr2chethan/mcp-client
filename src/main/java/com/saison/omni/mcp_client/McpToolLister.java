package com.saison.omni.mcp_client;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McpToolLister implements CommandLineRunner {

    private final List<McpSyncClient> mcpClients;

    @Autowired
    public McpToolLister(List<McpSyncClient> mcpClients) {
        this.mcpClients = mcpClients;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Discovering MCP Tools:");
        for (McpSyncClient client : mcpClients) {
            System.out.println("  Connected to MCP Client: " + client.getClientInfo().name());

            // Use the provider to get ToolCallbacks from the client
            SyncMcpToolCallbackProvider provider = new SyncMcpToolCallbackProvider(List.of(client));
            List<ToolCallback> toolCallbacks = List.of(provider.getToolCallbacks());

            if (toolCallbacks.isEmpty()) {
                System.out.println("    No tools found on this MCP client.");
            } else {
                for (ToolCallback toolCallback : toolCallbacks) {
                    ToolDefinition toolDefinition = toolCallback.getToolDefinition();
                    System.out.println("    Tool Name: " + toolDefinition.name());
                    System.out.println("      Description: " + toolDefinition.description());
                    System.out.println("      Input Schema: " + toolDefinition.inputSchema());
                    System.out.println("--------------------");
                }
            }
        }
    }
}