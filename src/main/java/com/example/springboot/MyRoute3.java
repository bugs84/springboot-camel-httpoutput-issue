package com.example.springboot;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MyRoute3 extends RouteBuilder {

    @Override
    public void configure() throws Exception {
//        from("timer:foo").to("log:bar");

        from("servlet:///http" +
                "?httpMethodRestrict=GET" +
                "&servletName=CamelServlet"
        )
                .log("YYYYYYYYYYYYYYYYYYYYYYEEEE Hello Camel2")
                .process(new MyProcessor())
                .log("YYY Done")
        ;

    }

    class MyProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            System.out.println("MY PROCESSSOR");
            exchange.getMessage().setBody("AAAAAAAAAAA");


        }
    }
}