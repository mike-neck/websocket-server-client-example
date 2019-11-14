package com.example.events;

public class Message implements Event {

  public final int messageId;
  public final String id;
  public final String user;
  public final String text;

  public Message(final int messageId, final String id, final String user, final String text) {
    this.messageId = messageId;
    this.id = id;
    this.user = user;
    this.text = text;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Message{");
    sb.append("id='").append(id).append('\'');
    sb.append(", user='").append(user).append('\'');
    sb.append(", text='").append(text).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
