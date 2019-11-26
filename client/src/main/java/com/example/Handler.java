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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.LongStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

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

    final Flux<WebSocketMessage> receive = session.receive();

//    final Mono<Void> first = onlyFirst(receive);
    final Sub sub = listen(receive);
    final Flux<Void> send = periodicSend(session);

    final Mono<Void> s1 = Flux.merge(started, sub.first).then();
    final Mono<Void> all = Flux.merge(sub.others, send).then();

    return s1.then(all);
  }

  private static class Sub {

    final Mono<String> first;
    final Flux<InBoundMessage> others;

    private Sub(final Mono<String> first,
        final Flux<InBoundMessage> others) {
      this.first = first;
      this.others = others;
    }
  }

  @SuppressWarnings("ConstantConditions")
  private Sub listen(Flux<WebSocketMessage> session) {
    final CompletableFuture<String> future = new CompletableFuture<>();
    final CompletableFuture<Flux<InBoundMessage>> fluxFuture = new CompletableFuture<>();

    final Flux<Long> index = Flux.fromStream(LongStream.iterate(0L, i -> i + 1).boxed());

    Flux.zip(index, session.map(message -> message.getPayloadAsText(StandardCharsets.UTF_8)).flatMap(str -> readJson(InBoundMessage.class, str)))
        .groupBy(t -> t.getT1() == 0, Tuple2::getT2)
        .subscribe(gf -> {
          logger.info("start subscription of receive: {}", gf.key());
          if (gf.key()) {
            gf.take(1L).single().map(m -> m.text)
                .doOnNext(str -> logger.info("single : {}", str))
                .subscribe(future::complete);
          } else {
            fluxFuture.complete(gf);
          }
        });
    final Mono<String> first = Mono.fromFuture(future).doOnNext(str -> logger.info("receive : single: {}", str));
    final Flux<InBoundMessage> others = Mono.fromFuture(fluxFuture).flatMapMany(it -> it).doOnNext(message -> logger.info("receive: {}", message));
    return new Sub(first, others);
  }

  private <T> Mono<T> readJson(Class<T> klass, String json) {
    try {
      final T object = objectMapper.readValue(json, klass);
      return Mono.just(object);
    } catch (JsonProcessingException e) {
      logger.info("received a message but cannot deserialize it: {}, error: {}", json, e.toString(),
          e);
      return Mono.empty();
    }
  }

  private Flux<Void> periodicSend(WebSocketSession session) {
    return Flux.interval(Duration.ofSeconds(5L), Duration.ofSeconds(10L))
        .map(Seq::new)
        .flatMap(this::writeJson)
        .map(json -> new OutBoundMessage(userName, json))
        .flatMap(this::writeJson)
        .doOnNext(json -> logger.info("send: {}", json))
        .map(session::textMessage)
        .flatMap(message -> session
            .send(Mono.just(message).doOnNext(m -> logger.info("send done: {}", message))));
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
