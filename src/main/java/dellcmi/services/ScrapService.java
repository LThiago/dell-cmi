package dellcmi.services;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ScrapService {

    @Autowired
    private WebClient client;

    public String initConnection(String username, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("j_username", username);
        credentials.put("j_password", password);

        String response = doLogin(credentials);

        return response;
    }

    private String doLogin(Map<String, String> credentials){
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        for(Map.Entry<String, String> field : credentials.entrySet()){
            formData.add(field.getKey(), field.getValue());
        }

        String response = this.client
                .post()
                .uri("/cs/login/j_security_check")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();;
        return response;
    }

}
