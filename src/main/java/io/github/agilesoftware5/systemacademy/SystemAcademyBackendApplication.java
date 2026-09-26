package io.github.agilesoftware5.systemacademy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SystemAcademyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SystemAcademyBackendApplication.class, args);
	}

}
