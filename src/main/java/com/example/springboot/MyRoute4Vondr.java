package com.example.springboot;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
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
public class MyRoute4Vondr extends RouteBuilder {

    ExecutorService executorService = Executors.newFixedThreadPool(10);


    @Override
    public void configure() throws Exception {
        CamelContext context = getContext();

        String parallelSplitterEndpoint = "direct:parallel4v";
        String synchronizerEndpoint = "direct:synchronizer4v";
        String httpOutputEndpoint = "direct:httpOutput4v";

        var correlationExpression = new ParallelCorrelationExpression();

        from("servlet:///http4Vondr" +
                "?httpMethodRestrict=GET" +
                "&servletName=CamelServlet"
        )
                .log("MyRoute4")
                .process(new CorrelationIdGenerator())
                .setHeader("scaler-sync-correlation", correlationExpression)
                .setHeader("JMSCorrelationID", correlationExpression)
                .to(parallelSplitterEndpoint)
        ;

        from(parallelSplitterEndpoint)
                .log("Direct endpoint executed")
                .multicast()
                .executorService(executorService)
                .parallelProcessing()
                .to(new String[]{synchronizerEndpoint})
        ;

        from(synchronizerEndpoint)
                .setHeader("scaler-sync-correlation", correlationExpression)
                .log("Synchronizer")
//                .process(new BreakPointProcessor())
                .aggregate(correlationExpression, new MyRoutesAggregator())
                .completionSize(1)
//                .process(new BreakPointProcessor())
                .to(httpOutputEndpoint);

        from(httpOutputEndpoint)
                .log("httpEndpoint start")
//                .process(new BreakPointProcessor())
                .process(new ProcessHttpOutput())
                .log("httpEndpoint end")
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
            outputStream.write(("Request number: "+ requestNumber + ",  timeInMillis: " + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
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


    static String correlationId = null;
    class CorrelationIdGenerator implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            correlationId = UUID.randomUUID().toString();
        }
    }


    class MyRoutesAggregator implements AggregationStrategy {

        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            return newExchange;
        }
    }

    /**
     * Correlation identifier that identifies which routes to aggregate in parallel synchronizer module.
     * In other words, synchronizer waits on multiple messages with ID that is returned by this expression.
     */
    class ParallelCorrelationExpression implements Expression {
        @Override
        public <T> T evaluate(Exchange exchange, Class<T> type) {
            String correlationId = MyRoute4Vondr.correlationId;
//            String correlationId = "static-correlation-id";
            return type.cast(correlationId);
        }


    }

}