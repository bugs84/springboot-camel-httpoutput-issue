package com.example.springboot;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpMessage;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Component
public class MyRouteMini extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("servlet:///httpMini" +
                "?httpMethodRestrict=GET" +
                "&servletName=CamelServlet"
        )
                .process(new ProcessHttpOutput())
        ;

    }

    class ProcessHttpOutput implements Processor {

        int requestNumber = 1;

        @Override
        public void process(Exchange exchange) throws Exception {
            exchange.getOut().getBody(HttpServletResponse.class);

            HttpServletResponse response = exchange.getOut(HttpMessage.class).getResponse();
            response.setStatus(200);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(("Request number: " + requestNumber + ",  timeInMillis: " + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
            requestNumber++;
            outputStream.flush();
            outputStream.close();
        }
    }

    class BreakPointProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            System.out.println("BreakPoint");
        }
    }

}