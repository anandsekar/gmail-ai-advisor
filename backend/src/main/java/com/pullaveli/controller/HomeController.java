package com.pullaveli.controller;

import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;
import com.pullaveli.service.GeminiService;
import com.pullaveli.service.GmailService;
import com.pullaveli.service.GmailServiceClient;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
public class HomeController {

    private final GmailService gmailService;
    private final GeminiService geminiService;

    public HomeController(GmailService gmailService, GeminiService geminiService) {
        this.gmailService = gmailService;
        this.geminiService = geminiService;
    }

    @GetMapping("/")
    public String home(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                       @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            String refreshToken = authorizedClient.getRefreshToken() != null ? 
                authorizedClient.getRefreshToken().getTokenValue() : "Not available (already granted?)";
            
            StringBuilder draftsHtml = new StringBuilder();
            try {
                // Create a client for this specific request/user
                GmailServiceClient gmailServiceClient = gmailService.newGmailServiceClient(authorizedClient.getAccessToken().getTokenValue());
                
                // Fetch drafts
                List<Draft> drafts = gmailService.listDrafts(gmailServiceClient);
                
                draftsHtml.append("<h3>Drafts (").append(drafts.size()).append("):</h3><ul>");
                
                // Limit to first 3 drafts to avoid hitting API limits or long wait times
                int count = 0;
                for (Draft draft : drafts) {
                    if (count >= 3) break;
                    
                    draftsHtml.append("<li><strong>Draft ID:</strong> ").append(draft.getId());
                    
                    // Fetch full draft content to get the snippet/body
                    try {
                        Draft fullDraft = gmailServiceClient.getDraft(draft.getId());
                        Message message = fullDraft.getMessage();

                        draftsHtml.append("<br><em>Snippet:</em> ").append(GmailServiceClient.getBodyFromMessage(message));
                        
                        // Analyze with Gemini
                        String analysis = geminiService.analyzeDraft(GmailServiceClient.getBodyFromMessage(message));
                        draftsHtml.append("<br><strong>AI Analysis:</strong> ").append(analysis);
                        
                    } catch (Exception e) {
                        draftsHtml.append("<br><em>Error analyzing draft: ").append(e.getMessage()).append("</em>");
                    }
                    
                    draftsHtml.append("</li><br>");
                    count++;
                }
                draftsHtml.append("</ul>");
                if (drafts.size() > 3) {
                    draftsHtml.append("<p><em>(Showing analysis for first 3 drafts only)</em></p>");
                }

            } catch (IOException | GeneralSecurityException e) {
                draftsHtml.append("<p>Error fetching drafts: ").append(e.getMessage()).append("</p>");
            }

            return "<h1>" + gmailService.getWelcomeMessage(principal.getAttribute("name")) + "</h1>" +
                   "<p>You are successfully authenticated with Google.</p>" +
                   "<p><strong>Access Token:</strong> " + authorizedClient.getAccessToken().getTokenValue() + "</p>" +
                   "<p><strong>Refresh Token:</strong> " + refreshToken + "</p>" +
                   draftsHtml.toString() +
                   "<br>" +
                   "<form action=\"/logout\" method=\"post\">" +
                   "  <button type=\"submit\">Logout</button>" +
                   "</form>";
        }
        return "Hello, Guest!";
    }
}
