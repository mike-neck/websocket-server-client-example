package com.example;

import com.example.handler.ExportingNotifications;
import com.example.handler.ComingNotifications;
import com.example.handler.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class Handler implements WebSocketHandler {

  private static final Logger logger = LoggerFactory.getLogger(Handler.class);

  private final ObjectMapper objectMapper;
  private final MessageService messageService;
  private final ExportingNotifications exportingNotifications;
  private final ComingNotifications comingNotifications;

  public Handler(
      final ObjectMapper objectMapper,
      final MessageService messageService,
      final ExportingNotifications exportingNotifications,
      final ComingNotifications comingNotifications) {
    this.objectMapper = objectMapper;
    this.messageService = messageService;
    this.exportingNotifications = exportingNotifications;
    this.comingNotifications = comingNotifications;
  }

  @Override
  public Mono<Void> handle(final WebSocketSession session) {
    logger.info("new user coming: {}, {}", session.getId(), session.getAttributes());
    final Session ss = new Session(objectMapper, session, messageService);
    final Flux<Void> send = ss.send(exportingNotifications);
    final Mono<Void> receive = ss.receive(comingNotifications);
    return send.zipWith(receive).then();
  }
}
