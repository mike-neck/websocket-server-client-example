package com.example;

import com.example.message.Hello;
import com.example.message.InBoundMessage;
import com.example.message.OutBoundMessage;
import com.example.message.Seq;
import com.example.message.UserName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketMessage.Type;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Handler implements WebSocketHandler {

  private static final Logger logger = LoggerFactory.getLogger(Handler.class);

  private final ObjectMapper objectMapper;
  private final UserName userName;

  Handler(final ObjectMapper objectMapper, final UserName userName) {
    this.objectMapper = objectMapper;
    this.userName = userName;
  }

  @Override
  public Mono<Void> handle(final WebSocketSession session) {
    final String id = session.getId();
    logger.info("connected, id: {}", id);
    final Hello hello = new Hello(userName);
    final Mono<WebSocketMessage> helloMessage = Mono.justOrEmpty(hello.toJson(objectMapper))
        .map(session::textMessage);
    final Mono<Void> started = session.send(helloMessage);

    final Mono<Void> listen = listen(session);
    final Mono<Void> send = periodicSend(session);

    return started.then(listen.zipWith(send).then());
  }

  private Mono<Void> listen(WebSocketSession session) {
    return session.receive()
        .filter(webSocketMessage -> webSocketMessage.getType() == Type.TEXT)
        .map(webSocketMessage -> webSocketMessage.getPayloadAsText(StandardCharsets.UTF_8))
        .flatMap(json -> readJson(InBoundMessage.class, json))
        .doOnNext(inBoundMessage -> logger.info("receive message: {}", inBoundMessage))
        .then();
  }

  private <T> Mono<T> readJson(Class<T> klass, String json) {
    try {
      final T object = objectMapper.readValue(json, klass);
      return Mono.just(object);
    } catch (JsonProcessingException e) {
      logger.info("received a message but cannot deserialize it: {}, error: {}", json, e.toString(), e);
      return Mono.empty();
    }
  }

  private Mono<Void> periodicSend(WebSocketSession session) {
    return Flux.interval(Duration.ofSeconds(5L), Duration.ofSeconds(10L))
        .map(Seq::new)
        .flatMap(this::writeJson)
        .map(json -> new OutBoundMessage(userName, json))
        .flatMap(this::writeJson)
        .doOnNext(message -> logger.info("send message: {}", message))
        .map(session::textMessage)
        .flatMap(message -> session.send(Mono.just(message)))
        .then();
  }

  private Mono<String> writeJson(Object object) {
    try {
      final String json = objectMapper.writeValueAsString(object);
      return Mono.just(json);
    } catch (JsonProcessingException e) {
      logger.info("failed to write json: {}, error: {}", object, e.toString(), e);
      return Mono.empty();
    }
  }
}
