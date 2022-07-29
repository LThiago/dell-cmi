package dellcmi.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Service;

import dellcmi.util.Curl;
import dellcmi.util.CurlWrapper;

@Service
public class ScrapService {

    private HttpClient httpClient = buildHttpClient();;

    private HttpClientContext clientContext;

    List<HttpCookie> listCookies = new ArrayList<HttpCookie>();

    //private String baseUrl = "http://owc-lqd.dsc.ufcg.edu.br:8081";
    private String baseUrl = "https://cowc-dev.dell.com";

    

    public String getLoginPageCurl(String username, String password) throws IOException{
        StringBuilder content = new StringBuilder();
            try {
    
                ProcessBuilder pb = new ProcessBuilder("curl", "-s", "-D", "-", "-o", "nul", "-L", "-X", "POST", "https://cowc-dev.dell.com/cs/login/j_security_check", "-H", "Content-Type: application/x-www-form-urlencoded", "--data-raw", "j_username=" + username + "&j_password=" + password);
                pb.redirectErrorStream(true);
    
                Process proc = pb.start();
    
                InputStream ins = proc.getInputStream();
    
                ArrayList<String> cookies = new ArrayList();
                BufferedReader read = new BufferedReader(new InputStreamReader(ins));
                StringBuilder sb = new StringBuilder();
                

                
                read.lines().forEach(line -> {
                    // System.out.println("line>" + line);
                    if (line.contains("Set-Cookie")){
                        listCookies.addAll(HttpCookie.parse(line));

                        cookies.add(line);
                    } else {
                        content.append(line);
                        content.append("\n");
                    }
                    sb.append(line);
                });
    
                read.close();
    
                cookies.forEach(line -> {
                    System.out.println(line);
                    sb.append(line);
                });
    
                proc.waitFor();
    
                int exitCode = proc.exitValue();
                //System.out.println("exit code::" + exitCode);
    
                proc.destroy();
    
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return content.toString();
        
    }

   
    public String initConnection(String username, String password) throws IOException {
        //this.httpClient = buildHttpClient();

        if (this.listCookies.size() == 0){
            getLoginPageCurl(username,password);
        }
       
        CookieStore cookieStore = this.clientContext.getCookieStore();
        this.listCookies.forEach( c -> {
            BasicClientCookie cookie = new BasicClientCookie(c.getName(), c.getValue());
            cookie.setDomain(c.getDomain());
            cookie.setSecure(c.getSecure());
            cookie.setAttribute("httponly", "true");
            cookieStore.addCookie(cookie);
        });
        
        Map<String, String> credentials = new HashMap<>();
        credentials.put("j_username", username);
        credentials.put("j_password", password);

        String uri = "/cs/login/j_security_check";
        System.out.print("%%% QUERY: " + baseUrl + uri);
        HttpPost loginRequest = createPostRequest(baseUrl + uri, credentials);
        
        HttpResponse response = this.httpClient.execute(loginRequest, this.clientContext);

        for (int i = 0; i < response.getAllHeaders().length; i++) {
            System.out.println(response.getAllHeaders()[i]);
        } 

        String content = this.getUserInfoJson();

        //String content = new BasicResponseHandler().handleResponse(response);
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
                    .setDefaultCookieStore(cookieStore)
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

    public String getUserInfoJson() throws IOException {
        String uri = "/cs/idcplg?IdcService=GET_USER_INFO&IsJson=1";
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

    public HttpGet getPageWithTrustSSL() {
        return createGetRequest("https://mms.nw.ru/");
    }
}
