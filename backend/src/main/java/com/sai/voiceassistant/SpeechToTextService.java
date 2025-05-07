package com.sai.voiceassistant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
public class SpeechToTextService {
    @Value("${wit.ai.token:}")
    private String witAiToken;

    @Value("${wit.ai.api.url:https://api.wit.ai/speech?v=20220622}")
    private String witAiApiUrl;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String transcribe(byte[] audioData) throws IOException {
        if (witAiToken == null || witAiToken.trim().isEmpty()) {
            throw new IllegalStateException("Wit.ai token is not configured");
        }
        if (audioData == null || audioData.length == 0) {
            throw new IllegalArgumentException("Audio data is empty");
        }
        String apiUrl = witAiApiUrl;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + witAiToken.trim());
        headers.setContentType(MediaType.parseMediaType("audio/wav"));
        HttpEntity<byte[]> entity = new HttpEntity<>(audioData, headers);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
        } catch (Exception ex) {
            throw new IOException("Wit.ai Speech API error: " + ex.getMessage(), ex);
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IOException("Wit.ai Speech API error: HTTP " + response.getStatusCodeValue() + " - " + response.getBody());
        }
        String respStr = response.getBody();
        String transcript = "";
        int idx = 0;
        while (idx < respStr.length()) {
            int start = respStr.indexOf('{', idx);
            if (start == -1) break;
            int braceCount = 0;
            int end = start;
            boolean inString = false;
            while (end < respStr.length()) {
                char c = respStr.charAt(end);
                if (c == '"') inString = !inString;
                if (!inString) {
                    if (c == '{') braceCount++;
                    else if (c == '}') braceCount--;
                }
                if (braceCount == 0 && end > start) break;
                end++;
            }
            if (braceCount == 0) {
                String objStr = respStr.substring(start, end + 1);
                try {
                    JSONObject obj = new JSONObject(objStr);
                    String text = obj.optString("text", "");
                    if (!text.isEmpty()) {
                        transcript = text;
                        if (obj.optBoolean("is_final", false)) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Ignore parse errors
                }
                idx = end + 1;
            } else {
                break;
            }
        }
        return transcript;
    }

    public String getGeminiResponse(String prompt, String apiKey) throws IOException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }
        String apiUrl = geminiApiUrl + "?key=" + apiKey.trim();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"") + "\"}]}]}";
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
        } catch (Exception ex) {
            throw new IOException("Gemini API error: " + ex.getMessage(), ex);
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IOException("Gemini API error: HTTP " + response.getStatusCodeValue() + " - " + response.getBody());
        }
        JSONObject obj = new JSONObject(response.getBody());
        if (obj.has("candidates")) {
            return obj.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .optString("text", "");
        }
        return "";
    }
}