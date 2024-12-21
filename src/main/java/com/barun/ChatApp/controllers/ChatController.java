package com.barun.ChatApp.controllers;

import com.barun.ChatApp.dto.MessageDto;
import com.barun.ChatApp.models.ChatMessage;
import com.barun.ChatApp.services.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {
    private final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageService chatMessageService;

    @MessageMapping("/chat.send")
    public void handleWebSocketMessage(@Payload MessageDto messageDto, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication authentication = (Authentication) headerAccessor.getUser();
            if (authentication == null) {
                logger.error("User not authenticated");
                throw new IllegalStateException("User not authenticated");
            }

            String senderUsername = authentication.getName();
            logger.info("Processing message from {}", senderUsername);

            String receiverUsername = messageDto.getReceiver();
            ChatMessage savedMessage = chatMessageService.sendMessage(senderUsername, receiverUsername, messageDto.getContent());

            MessageDto savedMessageDto = new MessageDto(
                    savedMessage.getSender().getUsername(),
                    savedMessage.getReceiver() != null ? savedMessage.getReceiver().getUsername() : savedMessage.getReceiverUsername(),
                    savedMessage.getContent()
            );

            logger.info("Sending message to sender's queue: {}", senderUsername);
            messagingTemplate.convertAndSendToUser(
                    senderUsername,
                    "queue/messages",
                    savedMessageDto
            );

            if (savedMessage.getReceiver() != null) {
                logger.info("Sending message to receiver's queue: {}", receiverUsername);
                messagingTemplate.convertAndSendToUser(
                        receiverUsername,
                        "queue/messages",
                        savedMessageDto
                );
            }
        } catch (Exception e) {
            logger.error("Error processing WebSocket message: ", e);
            throw e;
        }
    }

    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<List<MessageDto>> getChatHistory(Principal principal) {
        try {
            if (principal == null) {
                logger.error("User not authenticated for chat history");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            logger.info("Fetching chat history for user: {}", principal.getName());
            List<ChatMessage> messages = chatMessageService.getMessagesForUser(principal.getName());

            List<MessageDto> messageDtos = messages.stream()
                    .map(message -> new MessageDto(
                            message.getSender().getUsername(),
                            message.getReceiver() != null ? message.getReceiver().getUsername() : message.getReceiverUsername(),
                            message.getContent()
                    ))
                    .collect(Collectors.toList());

            logger.info("Retrieved {} messages for user {}", messageDtos.size(), principal.getName());
            return ResponseEntity.ok(messageDtos);
        } catch (Exception e) {
            logger.error("Error fetching chat history: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}