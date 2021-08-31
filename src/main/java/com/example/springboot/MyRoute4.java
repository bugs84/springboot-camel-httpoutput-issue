package com.example.springboot;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MyRoute4 extends RouteBuilder {

    ExecutorService executorService = Executors.newFixedThreadPool(10);


    @Override
    public void configure() throws Exception {
        CamelContext context = getContext();

        String parallelSplitterEndpoint = "direct:parallel";
        String synchronizerEndpoint = "direct:synchronizer";
        String httpOutputEndpoint  = "direct:httpOutput";


        from("servlet:///http4" +
                "?httpMethodRestrict=GET" +
                "&servletName=CamelServlet"
        )
                .log("YYYYYYYYYYYYYYYYYYYYYYEEEE Hello Camel2")
                .log("YYY Done")
                .to(parallelSplitterEndpoint)
        ;

        from(parallelSplitterEndpoint)
                .log("Direct endpoint executed")
                .multicast()
                .executorService(executorService)
                .parallelProcessing()
                .to(synchronizerEndpoint)
        ;

        from(synchronizerEndpoint)
                .log("Synchronizer")
                .to(httpOutputEndpoint);

        from(httpOutputEndpoint)
                .log("httpEndpoint start")
                .process(new ProcessHttpOutput())
                .log("httpEndpoint end")
                ;




    }

    class ProcessHttpOutput implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            exchange.getMessage().setBody("Body of response " + System.currentTimeMillis());
        }
    }
}