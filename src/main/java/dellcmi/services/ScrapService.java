package dellcmi.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScrapService {

    @Autowired
    private HttpClient httpClient;

    private String baseUrl = "http://owc-lqd.dsc.ufcg.edu.br:8081";

    public String initConnection(String username, String password) throws IOException {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("j_username", username);
        credentials.put("j_password", password);

        HttpPost loginRequest = createLoginRequest(credentials);
        HttpResponse response = this.httpClient.execute(loginRequest);
        
        String responseString = new BasicResponseHandler().handleResponse(response);
        return responseString;
    }

    private HttpPost createLoginRequest(Map<String, String> credentials){
        String uri = baseUrl + "/cs/login/j_security_check";

        HttpPost request = new HttpPost(uri);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");

        List <NameValuePair> formData = new ArrayList <>();
        for(Map.Entry<String, String> field : credentials.entrySet()){
            formData.add(new BasicNameValuePair(field.getKey(), field.getValue()));
        }

        request.setEntity(new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8));

        return request;
    }

}
