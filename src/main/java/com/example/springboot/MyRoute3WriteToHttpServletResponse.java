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
public class MyRoute3WriteToHttpServletResponse extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("servlet:///http3Servlet" +
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
//            exchange.getMessage().setBody("Body of response " + System.currentTimeMillis());

            HttpServletResponse response = exchange.getOut().getBody(HttpServletResponse.class);

            HttpMessage httpMessage = exchange.getOut(HttpMessage.class);
            httpMessage.getResponse().setStatus(200);
            ServletOutputStream outputStream = httpMessage.getResponse().getOutputStream();
            outputStream.write(("MyResposne"+System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8) );

            outputStream.close();

            int i = 0;
//            response.setStatus(202);
//            response.getOutputStream().write(("MyResposne"+System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8) );

        }
    }
}