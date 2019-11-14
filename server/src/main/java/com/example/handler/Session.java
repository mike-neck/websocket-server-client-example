package com.example.handler;

import com.example.MessageService;
import com.example.events.Event;
import com.example.events.NewMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Session {

  private final ObjectMapper objectMapper;
  private final WebSocketSession delegate;

  private final MessageService messageService;

  public Session(final ObjectMapper objectMapper,
      final WebSocketSession delegate, final MessageService messageService) {
    this.objectMapper = objectMapper;
    this.delegate = delegate;
    this.messageService = messageService;
  }

  public Flux<Void> send(ExportingNotifications exportingNotifications) {
    return delegate.receive()
        .map(WebSocketMessage::getPayloadAsText)
        .flatMap(this::decodeToNewMessage)
        .map(ms -> messageService.createMessage(delegate.getId(), ms))
        .doOnNext(exportingNotifications::addNewEvent)
        .doOnComplete(() -> exportingNotifications.leave(delegate.getId()))
        .flatMap(message -> Mono.empty());
  }

  public Mono<Void> receive(ComingNotifications comingNotifications) {
    final Flux<Event> events = comingNotifications.messages();
    final Flux<String> encoded = events.flatMap(this::encode);
    final Flux<WebSocketMessage> messages = encoded.map(delegate::textMessage);
    return delegate.send(messages);
  }

  private Mono<String> encode(Event event) {
    try {
      final String json = objectMapper.writeValueAsString(event);
      return Mono.just(json);
    } catch (IOException e) {
      return Mono.empty();
    }
  }

  private Mono<NewMessage> decodeToNewMessage(String json) {
    try {
      final NewMessage newMessage = objectMapper.readValue(json, NewMessage.class);
      return Mono.just(newMessage);
    } catch (IOException e) {
      return Mono.empty();
    }
  }
}
