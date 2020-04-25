package com.edson.gae_final;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@EnableResourceServer
@SpringBootApplication
public class GaeFinalApplication {

	public static void main(String[] args) {
		SpringApplication.run(GaeFinalApplication.class, args);
	}

}
