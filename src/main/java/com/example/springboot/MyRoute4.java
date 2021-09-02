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
public class MyRoute4 extends RouteBuilder {

    ExecutorService executorService = Executors.newFixedThreadPool(10);


    @Override
    public void configure() throws Exception {
        CamelContext context = getContext();

        String parallelSplitterEndpoint = "direct:parallel2";
        String synchronizerEndpoint = "direct:synchronizer2";
        String httpOutputEndpoint = "direct:httpOutput2";

        var correlationExpression = new ParallelCorrelationExpression();

        from("servlet:///http4" +
                "?httpMethodRestrict=GET" +
                "&servletName=CamelServlet"
        )
                .log("YYYYYYYYYYYYYYYYYYYYYYEEEE Hello Camel2")
                .log("YYY Done")
                .process(new CorrelationIdGenerator())
                .process(new BreakPointProcessor())
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
//                .aggregate(new ConstantExpression(), new MyRoutesAggregator())
                .aggregate(correlationExpression, new MyRoutesAggregator())
//                .completionPredicate(new Predicate() {
//                    @Override
//                    public boolean matches(Exchange exchange) {
//                        return true;
//                    }
//                })


//                .aggregate(new MyRoutesAggregator())
//                .header("scaler-sync-correlation")
//                .process(new BreakPointProcessor())
                .completionSize(1)
//                .ignoreInvalidCorrelationKeys()
//                .setHeader("scaler-sync-correlation", correlationExpression) // so next synchronizer knows
//                .process(new BreakPointProcessor())
                .log("AGGGGGGGGGGRRRRRRRRRRRREEEEEEEEEGGGGGG")


//                .aggregate(correlationExpression, new MyRoutesAggregator())
//                .aggregate(correlationExpression, new UseOriginalAggregationStrategy())
//                .header("scaler-sync-correlation")
//                .completionSize(1)
//                .setHeader("scaler-sync-correlation", correlationExpression)
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
//            exchange.getMessage().setBody("Body of response " + System.currentTimeMillis());

            exchange.getOut().getBody(HttpServletResponse.class);

            HttpServletResponse response = exchange.getOut(HttpMessage.class).getResponse();
            response.setStatus(200);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(("MyHttpSplit Resposne" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));

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

//            if (oldExchange != null) {
//                val oldExRoute = oldExchange.route
//                logger.trace("Aggregating oldExchange route ${oldExRoute.fullId} ($oldExchange)")
//                setAnyRouteFailedAndCompletedFlagsIfNeeded(oldExchange, newExchange)
//                newExchange.variablesToMerge.addAll(oldExchange.variablesToMerge)
//            }


            return newExchange;
        }
    }


    public class OrderItemStrategy implements AggregationStrategy {

        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

//               if (oldExchange == null) {
//
//                   Item newItem= newExchange.getIn().getBody(Item.class);
//                   System.out.println("Aggregate first item: " + newItem);
//
//                   Order currentOrder = new Order();
//                   currentOrder.setId("ORD"+System.currentTimeMillis());
//                   List currentItems = new ArrayList();
//
//                   currentItems.add(newItem);
//                   currentOrder.setItems(currentItems);
//                   currentOrder.setTotalPrice(newItem.getPrice());
//
//                   newExchange.getIn().setBody(currentOrder);
//
//                    // the first time we aggregate we only have the new exchange,
//                    // so we just return it
//                    return newExchange;
//                }
//
//                Order order = oldExchange.getIn().getBody(Order.class);
//                Item newItem= newExchange.getIn().getBody(Item.class);
//
//                System.out.println("Aggregate old items: " + order);
//                System.out.println("Aggregate new item: " + newItem);
//
//                order.getItems().add(newItem);
//
//                double totalPrice = order.getTotalPrice() + newItem.getPrice();
//                order.setTotalPrice(totalPrice);

            // return old as this is the one that has all the orders gathered until now
            return oldExchange;
        }


    }


    /**
     * Correlation identifier that identifies which routes to aggregate in parallel synchronizer module.
     * In other words, synchronizer waits on multiple messages with ID that is returned by this expression.
     */
    class ParallelCorrelationExpression implements Expression {
        @Override
        public <T> T evaluate(Exchange exchange, Class<T> type) {
//            val route = exchange.route
//                        val correlationId = "${route.jobId}-${route.jobRun}-$synchronizerId"
//                        logger.trace("Resolving correlation ID of route ${route.fullId} to $correlationId")
//            String fromRouteId = UUID.randomUUID().toString();// exchange.getFromRouteId();
            String fromRouteId = correlationId;
            return type.cast(fromRouteId);
        }


    }

}