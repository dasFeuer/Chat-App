package com.barun.ChatApp.services;

import com.barun.ChatApp.models.ChatMessage;
import com.barun.ChatApp.models.User;
import com.barun.ChatApp.repositories.ChatMessageRepository;
import com.barun.ChatApp.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ChatMessage sendMessage(String senderUsername, String receiverUsername, String content) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> {
                    logger.error("Sender not found: {}", senderUsername);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found");
                });

        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> {
                    logger.error("Receiver not found: {}", receiverUsername);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found");
                });

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        logger.info("Saving message from {} to {}", senderUsername, receiverUsername);

        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getChatHistory(String senderUsername, String receiverUsername) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> {
                    logger.error("Sender not found: {}", senderUsername);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found");
                });

        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> {
                    logger.error("Receiver not found: {}", receiverUsername);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found");
                });

        logger.info("Fetching chat history between {} and {}", senderUsername, receiverUsername);
        return chatMessageRepository.findBySenderIdAndReceiverIdOrderByTimestampAsc(
                sender.getId(), receiver.getId()
        );
    }
}
