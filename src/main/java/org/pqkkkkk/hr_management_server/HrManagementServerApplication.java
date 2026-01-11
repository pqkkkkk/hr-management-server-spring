package org.pqkkkkk.hr_management_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HrManagementServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrManagementServerApplication.class, args);
	}

}
