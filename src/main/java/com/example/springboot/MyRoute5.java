package com.example.springboot;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpMessage;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MyRoute5 extends RouteBuilder {
    private final ParallelCorrelationExpression correlationExpression = new ParallelCorrelationExpression();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private String correlationId = null;

    @Override
    public void configure() {
        String parallelSplitterEndpoint = "direct:splitter";
        String synchronizerEndpoint = "direct:synchronizer";
        String httpOutputEndpoint = "direct:httpOutput";

        // HTTP Input
        from("servlet:///http5?httpMethodRestrict=GET&servletName=CamelServlet")
                .log("Http request received")
                .process(new CorrelationIdGenerator())
                .to(parallelSplitterEndpoint);

        // Parallel Splitter
        from(parallelSplitterEndpoint)
                .log("Parallel Splitter")
                .multicast()
                .executorService(executorService)
                .parallelProcessing()
                .to(new String[]{synchronizerEndpoint});

        // Parallel Synchronizer
        from(synchronizerEndpoint)
                .log("Parallel Synchronizer")
                .setHeader("scaler-sync-correlation", correlationExpression)
                .aggregate(correlationExpression, new MyRoutesAggregator())
                .completionSize(1)
                .to(httpOutputEndpoint);

        // HTTP Output
        from(httpOutputEndpoint)
                .log("HTTP Output")
                .process(new ProcessHttpOutput());
    }

    private static class ProcessHttpOutput implements Processor {
        private int i = 0;

        @Override
        public void process(Exchange exchange) throws Exception {
            HttpServletResponse response = exchange.getMessage().getBody(HttpMessage.class).getResponse();

            response.setStatus(200);
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                outputStream.write(("Response " + i++).getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        }
    }

    private class CorrelationIdGenerator implements Processor {
        @Override
        public void process(Exchange exchange) {
            correlationId = UUID.randomUUID().toString();
        }
    }

    private static class MyRoutesAggregator implements AggregationStrategy {
        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            return newExchange;
        }
    }

    private class ParallelCorrelationExpression implements Expression {
        @Override
        public <T> T evaluate(Exchange exchange, Class<T> type) {
            return type.cast(correlationId);
        }
    }
}
