package com.example.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

@SuppressWarnings("WeakerAccess")
public class Hello {

  public final String user;

  public Hello(final UserName user) {
    this.user = user.value;
  }

  public String getText() {
    return "こんにちは、 " + user + " です！よろしくおねがいします。";
  }

  public Optional<String> toJson(ObjectMapper objectMapper) {
    try {
      final String json = objectMapper.writeValueAsString(this);
      return Optional.of(json);
    } catch (JsonProcessingException e) {
      return Optional.empty();
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Hello{");
    sb.append("user='").append(user).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
