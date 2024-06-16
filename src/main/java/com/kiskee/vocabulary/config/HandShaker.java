package com.kiskee.vocabulary.config;

import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Component
public class HandShaker extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(
            ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Витягнути JWT з cookies
        //        String jwt = CookieUtil.extractTokenFromCookie(request.getCookies());
        //
        //        // Провести аутентифікацію і отримати Authentication об'єкт
        //        Authentication authentication = auth(jwt);
        //
        //        // Зберегти Authentication в SecurityContext
        //        SecurityContext context = SecurityContextHolder.createEmptyContext();
        //        context.setAuthentication(authentication);
        //        SecurityContextHolder.setContext(context);
        //
        //        // Повернути Principal (Authentication object)
        //        return authentication;
        Principal principal = request.getPrincipal();
        //        System.out.println(request.getHeaders());
        //        System.out.println(principal);
        return principal;
    }
}
