package com.dodam.dicegame.dicegame;

import com.dodam.dicegame.dicegame.sockethandler.RoomWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new RoomWebSocketHandler(), "/ws/room")
                .setAllowedOrigins("*"); // 클라이언트 도메인을 설정 (CORS)
    }
}
