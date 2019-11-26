package com.example;

import com.example.events.Event;
import com.example.handler.ComingNotifications;
import com.example.handler.ExportingNotifications;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

@SpringBootApplication
public class ServerApp {

  private static final Logger logger = LoggerFactory.getLogger(ServerApp.class);

  public static void main(String[] args) {
    SpringApplication.run(ServerApp.class, args);
  }

  @Bean
  RouterFunction<ServerResponse> routerFunction() {
    return RouterFunctions.route()
        .GET("/",
            request -> ServerResponse.ok().body(Mono.just(Map.of("url", "http://localhost:8080")),
                new ParameterizedTypeReference<>() {
                }))
        .build();
  }

  @Bean
  ObjectMapper objectMapper() {
    return new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        .setSerializationInclusion(Include.NON_NULL);
  }

  @Bean
  UnicastProcessor<Event> unicastProcessor() {
    return UnicastProcessor.create();
  }

  @Bean
  ExportingNotifications exportingNotifications(UnicastProcessor<Event> unicastProcessor) {
    return new ExportingNotifications(unicastProcessor);
  }

  @Bean
  ComingNotifications comingNotifications(UnicastProcessor<Event> unicastProcessor) {
    final Flux<Event> flux = unicastProcessor.replay(20).autoConnect();
    return new ComingNotifications(flux);
  }

  @Bean
  public MessageService messageService() {
    logger.info("registering message service");
    return new MessageService();
  }

  @Bean
  HandlerMapping handlerMapping(Handler handler) {
    logger.info("registering handler");
    final Map<String, Handler> map = Map.of("/chat", handler);
    final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping(map);
    mapping.setOrder(-1);
    return mapping;
  }

  @Bean
  WebSocketService webSocketService() {
    final ReactorNettyRequestUpgradeStrategy strategy = new ReactorNettyRequestUpgradeStrategy();
    return new HandshakeWebSocketService(strategy);
  }

  @Bean
  WebSocketHandlerAdapter handlerAdapter(WebSocketService webSocketService) {
    return new WebSocketHandlerAdapter(webSocketService);
  }
}
