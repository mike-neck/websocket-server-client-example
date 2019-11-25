package com.example.message;

public class OutBoundMessage {

  public final String user;

  public final String text;

  public OutBoundMessage(UserName user, String text) {
    this(user.value, text);
  }

  private OutBoundMessage(final String user, final String text) {
    this.user = user;
    this.text = text;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("OutBoundMessage{");
    sb.append("user='").append(user).append('\'');
    sb.append(", text='").append(text).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
