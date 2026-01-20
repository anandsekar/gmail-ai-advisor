package com.pullaveli.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.ListDraftsResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class GmailServiceClient {
    private static final String APPLICATION_NAME = "Gmail AI Advisor";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final Gmail gmailService;

    public GmailServiceClient(String accessToken) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        HttpRequestInitializer requestInitializer = request -> {
            request.getHeaders().setAuthorization("Bearer " + accessToken);
        };

        gmailService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Draft> listDrafts() throws IOException {
        List<Draft> allDrafts = new ArrayList<>();
        String pageToken = null;
        do {
            ListDraftsResponse response = gmailService.users().drafts().list("me")
                    .setPageToken(pageToken)
                    .execute();
            List<Draft> drafts = response.getDrafts();
            if (drafts != null) {
                allDrafts.addAll(drafts);
            }
            pageToken = response.getNextPageToken();
        } while (pageToken != null);
        return allDrafts;
    }

    public Message getMessage(String messageId) throws IOException {
        return gmailService.users().messages().get("me", messageId).execute();
    }
    
    public Draft getDraft(String draftId) throws IOException {
        return gmailService.users().drafts().get("me", draftId).execute();
    }

    public static String getBodyFromMessage(Message message) {
        String body = null;
        if (message.getPayload() != null) {
            // Check if the payload has parts (Multipart)
            if (message.getPayload().getParts() != null) {
                body = getBodyFromParts(message.getPayload().getParts());
            }
            // Fallback: Check if the payload itself has data (Simple)
            else if (message.getPayload().getBody() != null && message.getPayload().getBody().getData() != null) {
                body = decodeData(message.getPayload().getBody().getData());
            }
        }
        return body == null ? "" : body;
    }

    // Recursively traverse parts to find text/plain or text/html
    private static String getBodyFromParts(List<MessagePart> parts) {
        String textPlain = null;
        String textHtml = null;

        for (MessagePart part : parts) {
            // If this part is itself multipart, recurse down
            if (part.getParts() != null) {
                String result = getBodyFromParts(part.getParts());
                if (result != null) return result;
            }

            // Check MimeType
            if (part.getMimeType().equalsIgnoreCase("text/plain")) {
                textPlain = decodeData(part.getBody().getData());
            } else if (part.getMimeType().equalsIgnoreCase("text/html")) {
                textHtml = decodeData(part.getBody().getData());
            }
        }

        // Priority: Return plain text if found, otherwise HTML
        return textPlain != null ? textPlain : textHtml;
    }

    // Helper to decode Base64URL encoded string
    private static String decodeData(String data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        // Gmail API uses Base64 URL (RFC 4648)
        return new String(Base64.getUrlDecoder().decode(data));
    }
}
