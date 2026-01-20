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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GmailService {

    private static final String APPLICATION_NAME = "Gmail AI Advisor";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public GmailServiceClient newGmailServiceClient(String accessToken) throws GeneralSecurityException, IOException {
        return new GmailServiceClient(accessToken);
    }

    public List<Draft> listDrafts(GmailServiceClient gmailServiceClient) throws IOException {
        return gmailServiceClient.listDrafts();
    }

    public Message getMessage(GmailServiceClient gmailServiceClient, String messageId) throws IOException {
        return gmailServiceClient.getMessage(messageId);
    }

    public String getWelcomeMessage(String name) {
        // In the future, this could fetch summary stats from Gmail
        return "Hello, " + name + "! Welcome to your Gmail AI Advisor.";
    }
}
