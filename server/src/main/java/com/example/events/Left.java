package com.example.events;

public class Left implements Event {

  public final String message = "user left";
  public final String id;

  public Left(final String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Left{");
    sb.append("message='").append(message).append('\'');
    sb.append(", id='").append(id).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
