package com.pullaveli.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                       @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            String refreshToken = authorizedClient.getRefreshToken() != null ? 
                authorizedClient.getRefreshToken().getTokenValue() : "Not available (already granted?)";
                
            return "<h1>Hello, " + principal.getAttribute("name") + "!</h1>" +
                   "<p>You are successfully authenticated with Google.</p>" +
                   "<p><strong>Access Token:</strong> " + authorizedClient.getAccessToken().getTokenValue() + "</p>" +
                   "<p><strong>Refresh Token:</strong> " + refreshToken + "</p>" +
                   "<br>" +
                   "<form action=\"/logout\" method=\"post\">" +
                   "  <button type=\"submit\">Logout</button>" +
                   "</form>";
        }
        return "Hello, Guest!";
    }
}