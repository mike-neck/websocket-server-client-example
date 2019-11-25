package com.example.message;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Seq {

  public static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  public long seq;
  public String time;

  public Seq(final long seq) {
    this.seq = seq;
    this.time = OffsetDateTime.now(ZoneOffset.UTC).format(formatter);
  }

  public OffsetDateTime timeAsOffsetDateTime() {
    return OffsetDateTime.parse(time, formatter);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Seq{");
    sb.append("seq=").append(seq);
    sb.append('}');
    return sb.toString();
  }
}
