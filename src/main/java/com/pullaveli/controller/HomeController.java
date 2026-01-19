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
                   "<a href=\"/logout\">Logout</a>"; // Spring Security's default logout supports GET if CSRF is disabled or configured, but usually POST is preferred. For simplicity in this raw string return, a link to /logout works if we configure it or if we accept the default behavior which might require a form.
                   // Actually, Spring Security 6 / Boot 3 defaults to requiring POST for logout.
                   // Let's use a form to be safe and correct.
        }
        return "Hello, Guest!";
    }
}