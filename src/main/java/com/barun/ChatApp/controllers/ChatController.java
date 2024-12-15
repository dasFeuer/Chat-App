package com.barun.ChatApp.controllers;

import com.barun.ChatApp.dto.MessageDto;
import com.barun.ChatApp.models.ChatMessage;
import com.barun.ChatApp.services.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody MessageDto messageDto) {
        logger.info("Sending message from {} to {}", messageDto.getSender(), messageDto.getReceiver());
        ChatMessage message = chatMessageService.sendMessage(
                messageDto.getSender(),
                messageDto.getReceiver(),
                messageDto.getContent()
        );
        logger.info("Message sent successfully from {} to {}", messageDto.getSender(), messageDto.getReceiver());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInUsername = authentication.getName();
        logger.info("Fetching chat history for user: {}", loggedInUsername);
        List<ChatMessage> messages = chatMessageService.getMessagesForUser(loggedInUsername);
        logger.info("Chat history fetched successfully for user: {}", loggedInUsername);
        return ResponseEntity.ok(messages);
    }
}