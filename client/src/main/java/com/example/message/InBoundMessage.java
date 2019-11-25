package com.example.message;

public class InBoundMessage {

  public long messageId;
  public String id;
  public String user;
  public String text;

  public InBoundMessage() {
  }

  public InBoundMessage(final long messageId, final String id, final String user, final String text) {
    this.messageId = messageId;
    this.id = id;
    this.user = user;
    this.text = text;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Message{");
    sb.append("messageId=").append(messageId);
    sb.append(", id='").append(id).append('\'');
    sb.append(", user='").append(user).append('\'');
    sb.append(", text='").append(text).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
