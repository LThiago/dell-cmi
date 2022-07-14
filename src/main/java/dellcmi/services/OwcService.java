package dellcmi.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.http.client.config.CookieSpecs;
import org.springframework.stereotype.Service;

@Service
public class OwcService {

    private CloseableHttpClient httpClient = buildHttpClient();

    private HttpClientContext clientContext;

    private String owcUrl = "https://ceowc-dev.dell.com";

    public String owcMainPage() throws IOException, ParseException{
        HttpGet request = new HttpGet(owcUrl + "/cs");

        CloseableHttpResponse response = this.httpClient.execute(request);

        System.out.println("PRINTING HEADERS...");
        for (int i = 0; i < response.getHeaders().length; i++) {
            System.out.println(response.getHeaders()[i]);
        }
        System.out.println("\n");
        System.out.println("PRINTING COOKIES...");
        CookieStore cookieStore = this.clientContext.getCookieStore();
        List<Cookie> cookies = cookieStore.getCookies();
        cookies.forEach( c -> {
            System.out.println(c);
        });

        String content = EntityUtils.toString(response.getEntity());

        return content;
    }
    public String owcLogin(String username, String password) throws IOException, ParseException {
        //this.httpClient = buildHttpClient();

        Map<String, String> credentials = new HashMap<>();
        credentials.put("j_username", username);
        credentials.put("j_password", password);

        String uri = "/cs/login/j_security_check";
        HttpPost loginRequest = createPostRequest(owcUrl + uri, credentials);
        HttpResponse response = this.httpClient.execute(loginRequest, this.clientContext);

        for (int i = 0; i < response.getHeaders().length; i++) {
            System.out.println(response.getHeaders()[i]);
        } 

        String content = EntityUtils.toString((HttpEntity) response);
        return content;
    }

    public CloseableHttpClient buildHttpClient() {
        BasicCookieStore cookieStore = new BasicCookieStore();
        this.clientContext = HttpClientContext.create();
        this.clientContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        try{
            SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial((chain, authType) -> true).build();

        SSLConnectionSocketFactory sslConnectionSocketFactory =
                new SSLConnectionSocketFactory(sslContext, new String[]
                        {"SSLv2Hello", "SSLv3", "TLSv1","TLSv1.1", "TLSv1.2" }, null,
                        NoopHostnameVerifier.INSTANCE);

        PoolingHttpClientConnectionManager connectionManager = new
                PoolingHttpClientConnectionManager(RegistryBuilder.
                <ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory).build());

        CloseableHttpClient client = HttpClients
                .custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        return client;  
        }catch (Exception e){
            throw new RuntimeException(e);
        }
            
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

    /* 
    public String getUserInfo() throws IOException {
        String uri = "/cs/idcplg?IdcService=GET_USER_INFO&IsSoap=1";
        HttpGet request = createGetRequest(baseUrl + uri);

        HttpResponse response = this.httpClient.execute(request, this.clientContext);

        String content = new BasicResponseHandler().handleResponse(response);
        JSONObject obj = XML.toJSONObject(content);

        return obj.toString(4);
    }

    private HttpGet createGetRequest(String uri){
        HttpGet request = new HttpGet(uri);
        return request;
    }

    

    public String getPageWithTrustSSL() throws IOException {
        HttpGet request = createGetRequest("https://ceowc-uat.dell.com/cs");
        HttpResponse response = this.httpClient.execute(request, this.clientContext);

        String content = new BasicResponseHandler().handleResponse(response);
        return content;
    }
    */
}
