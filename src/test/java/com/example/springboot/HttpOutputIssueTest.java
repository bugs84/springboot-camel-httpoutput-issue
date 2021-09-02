package com.example.springboot;

import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpOutputIssueTest/* extends CamelSpringTestSupport */{

    @Autowired
    private ServerProperties serverProperties;

//    @Autowired
//    private ServletWebServerApplicationContext webServerAppCtxt;

    @LocalServerPort
    int randomServerPort;

//        @LocalManagementPort
//        int randomManagementPort;

    @Test
    void configure() throws InterruptedException {
        Integer port = serverProperties.getPort();

//        int port2 = webServerAppCtxt.getWebServer().getPort();

        System.out.println("PORT: " + port + "   , 2="+randomServerPort);
        Thread.sleep(20_000);
    }

//    @Override
//    protected AbstractApplicationContext createApplicationContext() {
//        AbstractApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
//
//        return context;
//    }
}