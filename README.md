# INFO
Sample spring boot application with Apache Camel.

## Purpose
Demonstrate issue we have after upgrade to Camel 3.11.0

# Issue description

We are experiencing issue in our project after upgrading Apache Camel from version 3.10.0 to 3.11.0 (3.11.1 has the same issue).
The same route which worked without any problems in previous versions, does not work now. 
We managed to simplify the route from our project and simulate the problem on this simplified route.

## Steps to reproduce:

**A. by autotest**
  1. run test `src/test/java/com/example/springboot/HttpOutputIssueTest.java`

**B. manually:**
  1. Run Application.java
  2. Route configuration is located in HttpOutputIssue.java
  3. Make GET request to http://localhost:8080/camel/httpIssue

 

### Note
Version of Camel can be changed in `pom.xml` by property `<camel.version>`




Route looks like this:

```java 
String synchronizerEndpoint = "direct:synchronizer";
String httpOutputEndpoint = "direct:httpOutput";

// HTTP Input
from("servlet:///httpIssue?httpMethodRestrict=GET&servletName=CamelServlet")
        .log("Http request received")
        .process(new CorrelationIdGenerator())
        .to(synchronizerEndpoint);

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
``` 
 
In `ProcessHttpOutput` processor we are writing the response into the OutputStream:
 
```java 
private static class ProcessHttpOutput implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        HttpServletResponse response = exchange.getMessage().getBody(HttpMessage.class).getResponse();
        response.setStatus(200);
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(("Response").getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }
}
```
 
If we run this route on Camel version 3.11.0. We are getting NullPointerException during flushing / closing the stream. It works without any problems on 3.10.0.
 
The problem is if aggregation is in the route (without aggregation it works fine). After aggregation, it is not possible to write to the output stream.
 
 
Here is the repository with the simplified route (branch camel-minimal-sample):
https://github.com/bugs84/springboot-camel-httpoutput-issue/tree/camel-minimal-sample
 


