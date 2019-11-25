package com.example;

import com.example.message.UserName;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

@Component
public class WebSocketRunner implements CommandLineRunner {

  private final ObjectMapper objectMapper;
  private final UserName userName;

  public WebSocketRunner(
      final ObjectMapper objectMapper,
      final UserName userName) {
    this.objectMapper = objectMapper;
    this.userName = userName;
  }

  @Override
  public void run(final String... args) throws Exception {
    final ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
    final URI uri = URI.create("ws://localhost:8080/chat");
    final Mono<Void> mono = client.execute(uri, new Handler(objectMapper, userName));
    final CountDownLatch latch = new CountDownLatch(1);
    try(AutoCloseable ignored = mono.doOnTerminate(latch::countDown).subscribe()::dispose) {
      latch.await();
    }
  }
}
