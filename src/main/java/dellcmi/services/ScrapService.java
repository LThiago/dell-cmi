package dellcmi.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Service;

@Service
public class ScrapService {

    private HttpClient httpClient = buildHttpClient();;

    private HttpClientContext clientContext;

    private String baseUrl = "https://ceowc-uat.dell.com";

    public String initConnection(String username, String password) throws IOException {
        //this.httpClient = buildHttpClient();

        Map<String, String> credentials = new HashMap<>();
        credentials.put("j_username", username);
        credentials.put("j_password", password);

        String uri = "/cs/login/j_security_check";
        HttpPost loginRequest = createPostRequest(baseUrl + uri, credentials);
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

        try {
            return HttpClients.custom()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUserInfo() throws IOException {
        String uri = "/cs/idcplg?IdcService=GET_USER_INFO&IsSoap=1";
        HttpGet request = createGetRequest(baseUrl + uri);

        HttpResponse response = this.httpClient.execute(request, this.clientContext);

        String content = new BasicResponseHandler().handleResponse(response);
        JSONObject obj = XML.toJSONObject(content);

        return obj.toString(4);
    }

    public String getInitialPage() throws IOException {
        String uri = "/cs";
        HttpGet request = createGetRequest(baseUrl + uri);

        HttpResponse response = this.httpClient.execute(request, this.clientContext);

        String content = new BasicResponseHandler().handleResponse(response);
        

        return content;
    }

    public String getLoginPage() throws IOException {
        String uri = "/cs/idcplg?IdcService=LOGIN&Action=GetTemplatePage&Page=HOME_PAGE&Auth=Internet";
        HttpGet request = createGetRequest(baseUrl + uri);

        HttpResponse response = this.httpClient.execute(request, this.clientContext);

        String content = new BasicResponseHandler().handleResponse(response);
        

        return content;
    }

    

    private HttpGet createGetRequest(String uri){
        HttpGet request = new HttpGet(uri);
        return request;
    }

    private HttpPost createPostRequest(String uri, Map<String, String> body){
        HttpPost request = new HttpPost(uri);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");

        List <NameValuePair> formData = new ArrayList <>();
        for(Map.Entry<String, String> field : body.entrySet()){
            formData.add(new BasicNameValuePair(field.getKey(), field.getValue()));
        }

        request.setEntity(new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8));

        return request;
    }

    public String getPageWithTrustSSL(String username, String password) throws IOException, ClientProtocolException{

        Map<String, String> credentials = new HashMap<>();
        credentials.put("j_username", username);
        credentials.put("j_password", password);

        String uri = "/cs/login/j_security_check";
        HttpPost loginRequest = createPostRequest(baseUrl + uri, credentials);
        HttpResponse response = this.httpClient.execute(loginRequest, this.clientContext);

        String content = new BasicResponseHandler().handleResponse(response);

        return content;
    }
}
