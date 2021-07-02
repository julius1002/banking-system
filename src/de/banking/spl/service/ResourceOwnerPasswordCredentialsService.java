package de.banking.spl.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class ResourceOwnerPasswordCredentialsService implements OpenIdConnectService{
    private HttpClient client;

    private String authorizationServerUri;

    public ResourceOwnerPasswordCredentialsService(String authorizationServerUri) {
        this.client = HttpClient.newBuilder().build();
        this.authorizationServerUri = authorizationServerUri;
        }

    @Override
    public HttpResponse<String> requestAccessToken(String username, String password, String scope) throws IOException, InterruptedException {
        HttpRequest tokenRequest = HttpRequest.newBuilder().uri(URI.create(authorizationServerUri + "/token"))
                .POST(HttpRequest.BodyPublishers.ofString("username=" + username + "&password=" + password + "&scope=" + scope))
                .header("Authorization", "Basic MTpzZWN1cmU=")
                .header("Content-Type", "application/x-www-form-urlencoded").build();
        return client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Override
    public HttpResponse<String> requestUserInfo(String accessToken) throws IOException, InterruptedException {
        HttpRequest userInfoRequest = HttpRequest.newBuilder().uri(URI.create(authorizationServerUri + "/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .build();
        return client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());
    }
}
