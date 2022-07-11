package dellcmi.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Service;

@Service
public class ScrapService {

    private HttpClient httpClient = buildHttpClient();;

    private HttpClientContext clientContext;

    private String baseUrl = "http://owc-lqd.dsc.ufcg.edu.br:8081";

    public String initConnection(String username, String password) throws IOException {
        //this.httpClient = buildHttpClient();

        Map<String, String> credentials = new HashMap<>();
        credentials.put("j_username", username);
        credentials.put("j_password", password);

        String uri = "/cs/login/j_security_check";
        HttpPost loginRequest = createPostRequest(uri, credentials);
        HttpResponse response = this.httpClient.execute(loginRequest, this.clientContext);

        for (int i = 0; i < response.getAllHeaders().length; i++) {
            System.out.println(response.getAllHeaders()[i]);
        } 

        String content = new BasicResponseHandler().handleResponse(response);
        return content;
    }

    public HttpClient buildHttpClient() {
        BasicCookieStore cookieStore = new BasicCookieStore();
        this.clientContext = HttpClientContext.create();
        this.clientContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        return HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
    }

    public String getUserInfo() throws IOException {
        String uri = "/cs/idcplg?IdcService=GET_USER_INFO&IsSoap=1";
        HttpGet request = createGetRequest(uri);

        HttpResponse response = this.httpClient.execute(request, this.clientContext);

        String content = new BasicResponseHandler().handleResponse(response);
        JSONObject obj = XML.toJSONObject(content);

        return obj.toString(4);
    }

    private HttpGet createGetRequest(String uri){
        uri = baseUrl + uri;
        HttpGet request = new HttpGet(uri);
        return request;
    }

    private HttpPost createPostRequest(String uri, Map<String, String> body){
        uri = baseUrl + uri;

        HttpPost request = new HttpPost(uri);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");

        List <NameValuePair> formData = new ArrayList <>();
        for(Map.Entry<String, String> field : body.entrySet()){
            formData.add(new BasicNameValuePair(field.getKey(), field.getValue()));
        }

        request.setEntity(new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8));

        return request;
    }

}
