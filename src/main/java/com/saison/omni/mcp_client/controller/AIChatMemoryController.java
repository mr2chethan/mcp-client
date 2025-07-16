package com.saison.omni.mcp_client.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("ai-mem-chat")
@RestController
public class AIChatMemoryController {

    private final ChatClient chatClient;

    private final ChatMemory chatMemory;

    private final ChatMemoryRepository chatMemoryRepository;


    public AIChatMemoryController(ChatClient.Builder chatClientBuilder, ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(chatMemory)
                        .build())
                .build();
    }

    public record MessageResponse(UUID conversationId, String message) {};

    @PostMapping
    public MessageResponse chat(@RequestParam(required = false) UUID relatedConversationId,
                                @RequestBody String message) {
        // the following releates the conversation - ideally should be from the front end
        // when there is only one question in the conversation - then they are part of Default Id in the chat memory
        UUID currentConversationId = relatedConversationId == null ?  UUID.randomUUID() : relatedConversationId;
        MessageResponse messageResponse;
        if (relatedConversationId == null) {
            messageResponse =  new MessageResponse(currentConversationId, chatClient.prompt()
                    .user(message)
                    .call().content());
        } else {
            messageResponse =  new MessageResponse(currentConversationId, chatClient.prompt()
                    .user(message)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, currentConversationId))
                    .call().content());
        }
        return messageResponse;
    }

    @GetMapping("/history")
    public List<String> findAllConversation() {
        return chatMemoryRepository.findConversationIds();
    }

    @GetMapping("/history/{id}")
    public List<Message> findConversationById(@PathVariable String id) {
        return chatMemoryRepository.findByConversationId(id);
    }

    @DeleteMapping("/history/delete")
    public void deleteHistory() {
        findAllConversation().forEach(chatMemoryRepository::deleteByConversationId);
    }
}
