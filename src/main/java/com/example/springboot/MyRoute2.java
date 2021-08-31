package com.example.springboot;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MyRoute2 extends RouteBuilder {

    @Override
    public void configure() throws Exception {
//        from("timer:foo").to("log:bar");

//        from("servlet:///my" +
//                "?httpMethodRestrict=GET" +
//                //                            "&httpBinding=customHttpBinding" +
//                "&servletName=CamelServlet"
//        )
//                .log("Hello Camel2");
    }
}