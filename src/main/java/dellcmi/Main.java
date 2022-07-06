package dellcmi;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {

    private BasicCookieStore cookieStore;

    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }

    @Bean
    public HttpClient httpClient() {
        this.cookieStore = new BasicCookieStore();
        return HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
    }

}