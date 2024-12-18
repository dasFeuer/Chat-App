package com.barun.ChatApp.services;

import com.barun.ChatApp.models.ChatMessage;
import com.barun.ChatApp.models.User;
import com.barun.ChatApp.repositories.ChatMessageRepository;
import com.barun.ChatApp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ChatMessage sendMessage(String senderUsername, String receiverUsername, String content) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);

        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getChatHistory(String senderUsername, String receiverUsername) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        return chatMessageRepository.findBySenderIdAndReceiverIdOrderByTimestampAsc(
                sender.getId(), receiver.getId()
        );
    }

    public List<ChatMessage> getMessagesForUser(String username) {
        User loggedInUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return chatMessageRepository.findByReceiverIdAndSenderIdNotOrderByTimestampAsc(
                loggedInUser.getId(), loggedInUser.getId()
        );
    }

}
