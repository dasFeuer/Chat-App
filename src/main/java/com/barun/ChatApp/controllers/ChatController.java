package com.barun.ChatApp.controllers;

import com.barun.ChatApp.dto.MessageDto;
import com.barun.ChatApp.models.ChatMessage;
import com.barun.ChatApp.services.ChatMessageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendRestMessage(@RequestBody @Valid MessageDto messageDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String sender = auth.getName();
        ChatMessage message = sendAndNotifyMessage(sender, messageDto.getReceiver(), messageDto.getContent());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedInUsername = auth.getName();
        logger.info("Fetching chat history for user: {}", loggedInUsername);
        List<ChatMessage> messages = chatMessageService.getMessagesForUser(loggedInUsername);
        logger.info("Chat history fetched successfully for user: {}", loggedInUsername);
        return ResponseEntity.ok(messages);
    }

    @MessageMapping("/chat.send")
    @SendToUser("/queue/messages")
    public ChatMessage handleWebSocketMessage(@Payload @Valid MessageDto messageDto,
                                              SimpMessageHeaderAccessor headerAccessor) {
        Authentication auth = (Authentication) headerAccessor.getUser();
        assert auth != null;
        String sender = auth.getName();
        logger.info("Received WebSocket message from {}: {}", sender, messageDto);
        ChatMessage chatMessage = sendAndNotifyMessage(sender, messageDto.getReceiver(), messageDto.getContent());
        logger.info("Sending WebSocket message to {}: {}", messageDto.getReceiver(), chatMessage);
        return chatMessage;
    }

    private ChatMessage sendAndNotifyMessage(String sender, String receiver, String content) {
        logger.info("Sending message from {} to {}: {}", sender, receiver, content);
        ChatMessage savedMessage = chatMessageService.sendMessage(sender, receiver, content);
        messagingTemplate.convertAndSendToUser(receiver, "/queue/messages", savedMessage);
        logger.info("Message sent successfully to user queue: {}", receiver);
        return savedMessage;
    }

//    @MessageMapping("/chat.send")
//    public void sendMessage(@Payload @Valid MessageDto messageDto,
//                            SimpMessageHeaderAccessor headerAccessor) {
//        try {
//            String sender = Objects.requireNonNull(headerAccessor.getUser()).getName();
//
//            logger.info("Sending message from {} to {}: {}",
//                    sender, messageDto.getReceiver(), messageDto.getContent());
//
//            ChatMessage savedMessage = chatMessageService.sendMessage(
//                    sender,
//                    messageDto.getReceiver(),
//                    messageDto.getContent()
//            );
//
//            messagingTemplate.convertAndSendToUser(
//                    messageDto.getReceiver(),
//                    "/queue/messages",
//                    savedMessage
//            );
//
//            logger.info("Message sent successfully to user queue: {}", messageDto.getReceiver());
//        } catch (Exception e) {
//            logger.error("Error processing WebSocket message: {}", e.getMessage(), e);
//        }
//    }
//
//
//    @PostMapping("/send")
//    public ResponseEntity<ChatMessage> sendMessage(@RequestBody MessageDto messageDto) {
//        logger.info("Sending message from {} to {}", messageDto.getSender(), messageDto.getReceiver());
//        ChatMessage message = chatMessageService.sendMessage(
//                messageDto.getSender(),
//                messageDto.getReceiver(),
//                messageDto.getContent()
//        );
//        logger.info("Message sent successfully from {} to {}", messageDto.getSender(), messageDto.getReceiver());
//        return ResponseEntity.ok(message);
//    }
//
//    @GetMapping("/history")
//    public ResponseEntity<List<ChatMessage>> getChatHistory() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String loggedInUsername = authentication.getName();
//        logger.info("Fetching chat history for user: {}", loggedInUsername);
//        List<ChatMessage> messages = chatMessageService.getMessagesForUser(loggedInUsername);
//        logger.info("Chat history fetched successfully for user: {}", loggedInUsername);
//        return ResponseEntity.ok(messages);
//    }


}