package edu.uclm.esi.fakeaccountsbe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // prefijo para enviar
        config.setApplicationDestinationPrefixes("/app"); // prefijo para recibir
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket") // endpoint de conexión
                .setAllowedOrigins("http://localhost:4200")
                .withSockJS(); // Soporte para navegadores antiguos
    }
}
