package com.example.handler;

import com.example.events.Event;
import com.example.events.Left;
import org.reactivestreams.Subscriber;

public class ExportingNotifications {

  private final Subscriber<Event> subscriber;

  public ExportingNotifications(final Subscriber<Event> subscriber) {
    this.subscriber = subscriber;
  }

  void addNewEvent(Event event) {
    subscriber.onNext(event);
  }

  void leave(String id) {
    subscriber.onNext(new Left(id));
  }
}
