package com.aguardientes.azarcafetero.parques_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ParquesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParquesServiceApplication.class, args);
	}

}
