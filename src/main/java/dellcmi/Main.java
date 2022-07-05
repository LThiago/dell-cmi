package dellcmi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@SpringBootApplication
public class Main {

    private String baseUrl = "http://owc-lqd.dsc.ufcg.edu.br:8081";

    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
//                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                .build();
    }

}