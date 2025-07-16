package com.saison.omni.mcp_client.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("mcp-chat")
@RestController
public class McpChatController {

    private final ChatClient chatClient;

    private final SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;

    public McpChatController(ChatClient.Builder chatClientBuilder,
                             @Qualifier("my-mcp-server-callback-tool-provider") SyncMcpToolCallbackProvider syncMcpToolCallbackProvider) {
        this.chatClient = chatClientBuilder.build();
        this.syncMcpToolCallbackProvider = syncMcpToolCallbackProvider;
    }

    @PostMapping
    public String chat(@RequestBody String message) {
        return chatClient.prompt()
                .user(message)
                .toolCallbacks(syncMcpToolCallbackProvider)
                .call().content();
    }
}
