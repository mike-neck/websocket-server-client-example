package com.example;

import com.example.events.Message;
import com.example.events.NewMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageService {

  private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

  private final AtomicInteger idGenerator = new AtomicInteger(0);

  private final List<Message> messages;

  public MessageService() {
    this.messages = new ArrayList<>();
  }

  public Message createMessage(String id, NewMessage newMessage) {
    final Message message = new Message(idGenerator.incrementAndGet(), id, newMessage.user,
        newMessage.text);
    messages.add(message);
    logger.info("create message: {}", message);
    return message;
  }
}
