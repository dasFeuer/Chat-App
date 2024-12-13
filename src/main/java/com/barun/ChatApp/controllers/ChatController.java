package com.barun.ChatApp.controllers;

import com.barun.ChatApp.models.ChatMessage;
import com.barun.ChatApp.services.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(
            @RequestParam String senderUsername,
            @RequestParam String receiverUsername,
            @RequestParam String content
    ) {
        logger.info("Sending message from {} to {}", senderUsername, receiverUsername);
        ChatMessage message = chatMessageService.sendMessage(
                senderUsername, receiverUsername, content
        );
        logger.info("Message sent successfully from {} to {}", senderUsername, receiverUsername);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @RequestParam String senderUsername,
            @RequestParam String receiverUsername
    ) {
        logger.info("Fetching chat history between {} and {}", senderUsername, receiverUsername);
        List<ChatMessage> messages = chatMessageService.getChatHistory(
                senderUsername, receiverUsername
        );
        logger.info("Chat history fetched successfully between {} and {}", senderUsername, receiverUsername);
        return ResponseEntity.ok(messages);
    }
}
