package de.system.banking.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ResourceOwnerPasswordCredentialsService implements OpenIdConnectService {

    private HttpClient client;

    private String authorizationServerUri;

    public ResourceOwnerPasswordCredentialsService(String authorizationServerUri) {
        this.client = HttpClient.newBuilder().build();
        this.authorizationServerUri = authorizationServerUri;
    }

    @Override
    public HttpResponse<String> requestAccessToken(String username, String password, String scope) throws IOException, InterruptedException {
        var tokenRequest = HttpRequest.newBuilder().uri(URI.create(authorizationServerUri + "/token"))
                .POST(HttpRequest.BodyPublishers.ofString("username=" + username + "&password=" + password + "&scope=" + scope))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + password).getBytes(StandardCharsets.UTF_8)))
                .header("Content-Type", "application/x-www-form-urlencoded").build();
        return client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Override
    public HttpResponse<String> requestUserInfo(String accessToken) throws IOException, InterruptedException {
        var userInfoRequest = HttpRequest.newBuilder().uri(URI.create(authorizationServerUri + "/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .build();
        return client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());
    }
}
