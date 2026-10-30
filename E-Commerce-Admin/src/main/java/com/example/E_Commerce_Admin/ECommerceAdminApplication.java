package com.example.E_Commerce_Admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.boot.persistence.autoconfigure.EntityScan("com.example.E_Commerce.model")
@SpringBootApplication
public class ECommerceAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceAdminApplication.class, args);
	}

}
