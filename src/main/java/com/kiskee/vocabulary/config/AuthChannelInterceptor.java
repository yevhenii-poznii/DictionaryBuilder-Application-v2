package com.kiskee.vocabulary.config;

import java.security.Principal;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ImmutableMessageChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthChannelInterceptor extends ImmutableMessageChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Principal user = accessor.getUser();
        //        System.out.println(message.getHeaders());

        if (user instanceof Authentication) {
            // Зберегти аутентифікацію в SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication((Authentication) user);
        }

        return message;
    }
}
