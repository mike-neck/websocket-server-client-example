package com.example;

import com.example.message.UserName;
import net.moznion.gimei.Gimei;
import net.moznion.gimei.name.Name;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClientApp {

  public static void main(String[] args) {
    SpringApplication.run(ClientApp.class);
  }

  @Bean
  UserName userName() {
    final Name name = Gimei.generateName();
    final String kanji = name.first().kanji();
    return new UserName(kanji);
  }
}
