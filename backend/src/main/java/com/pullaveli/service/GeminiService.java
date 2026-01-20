package com.pullaveli.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    public String analyzeDraft(String draftContent) {
        try {
            Client client = Client.builder().apiKey(apiKey).build();
            
            String prompt = "Analyze the following email draft and provide suggestions on how to make it more effective. " +
                    "Focus on clarity, tone, and call to action. Return a concise summary of improvements.\n\n" +
                    "Draft Content:\n" + draftContent;

            GenerateContentResponse response = client.models.generateContent("gemini-2.0-flash", prompt, null);
            
            return response.text();
        } catch (Exception e) {
            return "Error analyzing draft: " + e.getMessage();
        }
    }
}
