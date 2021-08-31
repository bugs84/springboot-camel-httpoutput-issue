package com.example.springboot;

import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfiguration {

    @Autowired
    CamelContext camelContext;

    @Bean
    Integer myNumber() {
        
        return 6;
    }
}
