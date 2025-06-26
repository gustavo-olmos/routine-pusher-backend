package com.routine.pusher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.routine.pusher")
@EntityScan(basePackages = "com.routine.pusher.core.domain")
public class RoutinePusherApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoutinePusherApplication.class, args);
	}

}

