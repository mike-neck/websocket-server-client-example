package com.example.events;

public class NewMessage {

  public String user;
  public String text;

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("NewMessage{");
    sb.append("user='").append(user).append('\'');
    sb.append(", text='").append(text).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
