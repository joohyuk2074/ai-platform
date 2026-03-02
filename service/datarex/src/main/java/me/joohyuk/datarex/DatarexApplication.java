package me.joohyuk.datarex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DatarexApplication {

  static void main(String[] args) {
    SpringApplication.run(DatarexApplication.class, args);
  }

}
