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
            String receiverUsername = messageDto.getReceiver(); // This is correct
            logger.info("Processing message from {} to {}", senderUsername, receiverUsername);

            logger.debug("Received MessageDTO: sender={}, receiver={}, content={}",
                    messageDto.getSender(), messageDto.getReceiver(), messageDto.getContent());

            ChatMessage savedMessage = chatMessageService.sendMessage(senderUsername, receiverUsername, messageDto.getContent());

            logger.debug("Saved message: id={}, sender={}, receiver={}, receiverUsername={}",
                    savedMessage.getId(),
                    savedMessage.getSender().getUsername(),
                    savedMessage.getReceiver() != null ? savedMessage.getReceiver().getUsername() : "null",
                    savedMessage.getReceiverUsername());

            MessageDto savedMessageDto = new MessageDto(
                    savedMessage.getId(),
                    savedMessage.getSender().getUsername(),
                    savedMessage.getReceiver() != null ? savedMessage.getReceiver().getUsername() : savedMessage.getReceiverUsername(),
                    savedMessage.getContent(),
                    "CREATE"
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
                            message.getId(),
                            message.getSender().getUsername(),
                            message.getReceiver() != null ? message.getReceiver().getUsername() : message.getReceiverUsername(),
                            message.getContent(),
                            null
                    ))
                    .collect(Collectors.toList());

            logger.info("Retrieved {} messages for user {}", messageDtos.size(), principal.getName());
            return ResponseEntity.ok(messageDtos);
        } catch (Exception e) {
            logger.error("Error fetching chat history: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @MessageMapping("/chat.update")
    public void handleMessageUpdate(@Payload MessageDto messageDto, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication authentication = (Authentication) headerAccessor.getUser();
            if (authentication == null) {
                logger.error("User not authenticated");
                throw new IllegalStateException("User not authenticated");
            }

            String username = authentication.getName();
            logger.info("Processing message update from {}", username);

            ChatMessage updatedMessage = chatMessageService.updateMessage(messageDto.getId(), messageDto.getContent(), username);

            MessageDto updatedMessageDto = new MessageDto(
                    updatedMessage.getId(),
                    updatedMessage.getSender().getUsername(),
                    updatedMessage.getReceiver() != null ? updatedMessage.getReceiver().getUsername() : updatedMessage.getReceiverUsername(),
                    updatedMessage.getContent(),
                    "UPDATE"
            );

            messagingTemplate.convertAndSendToUser(
                    username,
                    "queue/messages",
                    updatedMessageDto
            );

            if (updatedMessage.getReceiver() != null) {
                messagingTemplate.convertAndSendToUser(
                        updatedMessage.getReceiver().getUsername(),
                        "queue/messages",
                        updatedMessageDto
                );
            }
        } catch (Exception e) {
            logger.error("Error processing message update: ", e);
            throw e;
        }
    }

    @MessageMapping("/chat.delete")
    public void handleMessageDelete(@Payload MessageDto messageDto, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication authentication = (Authentication) headerAccessor.getUser();
            if (authentication == null) {
                logger.error("User not authenticated");
                throw new IllegalStateException("User not authenticated");
            }

            String username = authentication.getName();
            logger.info("Processing message deletion from {}", username);

            chatMessageService.deleteMessage(messageDto.getId(), username);

            messageDto.setAction("DELETE");

            messagingTemplate.convertAndSendToUser(
                    username,
                    "queue/messages",
                    messageDto
            );

            if (messageDto.getReceiver() != null) {
                messagingTemplate.convertAndSendToUser(
                        messageDto.getReceiver(),
                        "queue/messages",
                        messageDto
                );
            }
        } catch (Exception e) {
            logger.error("Error processing message deletion: ", e);
            throw e;
        }
    }
}


