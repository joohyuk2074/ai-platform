package me.joohyuk.datahub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DatahubApplication {

  static void main(String[] args) {
    SpringApplication.run(DatahubApplication.class, args);
  }

}
