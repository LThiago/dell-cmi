package dellcmi.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContexts;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class AuthenticationController {
    private final String CLIENT_ID = "fb593e1a-aeba-4518-83b7-90f8c69d1f56";
    private final String SECRET = "0a43b674-03fe-454b-8cd4-15a5834eb270";
    private final String ssoTokenRoute = "https://appsso.login.sr3.pcf.dell.com/oauth/token";
    private final String ssoUserInfoRoute = "https://appsso.login.sr3.pcf.dell.com/userinfo";

    @CrossOrigin
    @GetMapping("/authenticateUser")
    public String authenticateUser(@RequestParam String code, @RequestParam String redirectUri) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, ParseException {
       String clientIdAndSecret = CLIENT_ID + ":" + SECRET;
       byte[] byteArray = clientIdAndSecret.getBytes(StandardCharsets.US_ASCII);
       String clientBasicAuthorization = "Basic " + Base64.getEncoder().encodeToString(byteArray);

       UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(ssoTokenRoute)
               .queryParam("grant_type", "authorization_code")
               .queryParam("code", code)
               .queryParam("redirect_uri", redirectUri);

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
                .build();

        HttpGet request = new HttpGet(builder.toUriString());
        request.setHeader(HttpHeaders.AUTHORIZATION, clientBasicAuthorization);
        HttpEntity response = client.execute(request).getEntity();


        String content = EntityUtils.toString(response);

        String acessToken = new JSONObject(content).getString("access_token");
        
        UriComponentsBuilder builderUserInfo = UriComponentsBuilder.fromUriString(ssoUserInfoRoute);
        HttpGet requestUserInfo = new HttpGet(builderUserInfo.toUriString());
        requestUserInfo.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + acessToken);
        HttpEntity responseUserInfo = client.execute(requestUserInfo).getEntity();
        String contentUserInfo = EntityUtils.toString(responseUserInfo);

        return contentUserInfo;
    }
}
