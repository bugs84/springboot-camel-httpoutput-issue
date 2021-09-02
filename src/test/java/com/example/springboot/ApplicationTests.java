package com.example.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {

	@Test
	void contextLoads() throws InterruptedException {
		System.out.println("Test");
		Thread.sleep(20_000);
	}

}
