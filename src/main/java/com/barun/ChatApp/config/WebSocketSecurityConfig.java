package com.barun.ChatApp.config;

import com.barun.ChatApp.controllers.ChatController;
import com.barun.ChatApp.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketSecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;  // Reuse existing JwtTokenProvider
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);

                        try {
                            if (jwtTokenProvider.validateToken(token)) {
                                String username = jwtTokenProvider.getUsername(token);
                                String role = jwtTokenProvider.getRole(token).name();

                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(
                                                username,
                                                null,
                                                Collections.singletonList(new SimpleGrantedAuthority(role))
                                        );
                                accessor.setUser(auth);
                                logger.info("WebSocket connection authenticated for user: {}", username);
                            } else {
                                logger.error("Invalid or expired JWT token");
                                throw new IllegalStateException("Invalid JWT Token");
                            }
                        } catch (Exception e) {
                            logger.error("WebSocket authentication failed: {}", e.getMessage());
                            throw new IllegalArgumentException("Authentication failed", e);
                        }
                    } else {
                        logger.error("Authorization header missing or invalid");
                        throw new IllegalArgumentException("Authorization header missing");
                    }
                }
                return message;
            }
        });
    }


//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(new ChannelInterceptor() {
//            @Override
//            public Message<?> preSend(Message<?> message, MessageChannel channel) {
//                StompHeaderAccessor accessor =
//                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//                    String token = accessor.getFirstNativeHeader("Authorization");
//                    if (token != null && token.startsWith("Bearer ")) {
//                        token = token.substring(7);
//
//                        // Use your existing JwtTokenProvider methods
//                        if (jwtTokenProvider.validateToken(token)) {
//                            String username = jwtTokenProvider.getUsername(token);
//                            String role = jwtTokenProvider.getRole(token).name();
//
//                            UsernamePasswordAuthenticationToken auth =
//                                    new UsernamePasswordAuthenticationToken(
//                                            username,
//                                            null,
//                                            Collections.singletonList(new SimpleGrantedAuthority(role))
//                                    );
//                            accessor.setUser(auth);
//                        }
//                    }
//                }
//                return message;
//            }
//        });
//    }
}