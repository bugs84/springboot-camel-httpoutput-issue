package com.example.springboot;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static com.jcabi.http.Request.GET;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpOutputIssueTest {

    @LocalServerPort
    int port;

    /**
     * This test works with Camel 3.10.0, but fails with 3.11.0 and 3.11.1
     * version can be changed in pom.xml in camel.version property
     *
     * Looks like some sort of issue with aggregator
     **/
    @Test
    void httpIssue() throws Exception {
        String urlString = "http://localhost:" + port + "/camel/httpIssue";
        new JdkRequest(urlString)
                .method(GET)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HTTP_OK)
                .assertBody(equalTo("Response 1"))
        ;
    }

}