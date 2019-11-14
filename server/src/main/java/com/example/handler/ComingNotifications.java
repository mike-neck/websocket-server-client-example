package com.example.handler;

import com.example.events.Event;
import reactor.core.publisher.Flux;

public class ComingNotifications {

  private final Flux<Event> publishers;

  public ComingNotifications(final Flux<Event> publishers) {
    this.publishers = publishers;
  }

  Flux<Event> messages() {
    return publishers;
  }
}
