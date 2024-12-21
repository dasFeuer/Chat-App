package com.barun.ChatApp.services;

import com.barun.ChatApp.models.ChatMessage;
import com.barun.ChatApp.models.User;
import com.barun.ChatApp.repositories.ChatMessageRepository;
import com.barun.ChatApp.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(UserRepository userRepository, ChatMessageRepository chatMessageRepository) {
        this.userRepository = userRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public List<ChatMessage> getMessagesForUser(String username) {
        logger.info("Fetching messages for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", username);
                    return new RuntimeException("User not found");
                });

        return chatMessageRepository.findBySenderOrReceiverOrderByTimestampDesc(user, user);
    }

    @Transactional
    public ChatMessage sendMessage(String senderUsername, String receiverUsername, String content) {
        logger.info("Sending message from {} to {}", senderUsername, receiverUsername);
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> {
                    logger.error("Sender not found: {}", senderUsername);
                    return new RuntimeException("Sender not found");
                });

        Optional<User> receiverOptional = userRepository.findByUsername(receiverUsername);

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setContent(content);

        if (receiverOptional.isPresent()) {
            message.setReceiver(receiverOptional.get());
        } else {
            logger.warn("Receiver not found: {}. Storing message without receiver.", receiverUsername);
            message.setReceiverUsername(receiverUsername);
        }

        ChatMessage savedMessage = chatMessageRepository.save(message);
        logger.info("Message saved successfully: {}", savedMessage.getId());
        return savedMessage;
    }
}




