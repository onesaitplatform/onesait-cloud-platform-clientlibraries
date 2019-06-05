package com.minsait.onesait.platform.testwebsecurityclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.minsait.onesait.platform"})
public class TestWebSecurityClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestWebSecurityClientApplication.class, args);
	}

}
