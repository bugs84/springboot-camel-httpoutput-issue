package com.example.springboot;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MyComponent {

    @PostConstruct
    public void postConstruct(){
//        System.out.println("AAAAAAAAAAAAAAA");
    }
}
