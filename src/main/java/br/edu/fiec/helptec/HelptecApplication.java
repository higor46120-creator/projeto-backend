package br.edu.fiec.helptec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class HelptecApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelptecApplication.class, args);
	}

}
