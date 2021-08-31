package com.example.springboot;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//	@Bean
//	HttpBinding customHttpBinding() {
//		return new DefaultHttpBinding();
//	}
//			registry.bind(CustomHttpBinding.BINDING_NAME, CustomHttpBinding())

    @Bean
    ServletRegistrationBean<CamelHttpTransportServlet> camelServletRegistration() {
        CamelHttpTransportServlet servlet = camelServlet();
        var servletRegistrationBean = new ServletRegistrationBean(servlet);
        servletRegistrationBean.setName("CamelServlet");
        servletRegistrationBean.setOrder(2);
//        servletRegistrationBean.addUrlMappings("/rest/api/submit-job/*")
        servletRegistrationBean.addUrlMappings("/camel/*");
        return servletRegistrationBean;
    }

    @Bean
    CamelHttpTransportServlet camelServlet() {
      return new CamelHttpTransportServlet();
    }

}
