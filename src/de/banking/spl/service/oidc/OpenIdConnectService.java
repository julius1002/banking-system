package de.banking.spl.service.oidc;

import java.io.IOException;
import java.net.http.HttpResponse;

public interface OpenIdConnectService {

    HttpResponse<String> requestAccessToken(String username, String password, String scope) throws IOException, InterruptedException;

    HttpResponse<String> requestUserInfo(String accessToken) throws IOException, InterruptedException;
}
