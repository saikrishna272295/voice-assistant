package com.sai.voiceassistant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.sai.voiceassistant.SpeechToTextService;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/api/audio")
public class AudioController {
    @Autowired
    private SpeechToTextService speechToTextService;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @PostMapping("/transcribe")
    public ResponseEntity<?> transcribeAudio(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded");
        }
        try {
            byte[] audioData = file.getBytes();
            String transcript = speechToTextService.transcribe(audioData);
            if (transcript == null || transcript.trim().isEmpty()) {
                return ResponseEntity.ok("Could not transcribe audio.");
            }
            String geminiResponse = speechToTextService.getGeminiResponse(transcript, geminiApiKey);
            return ResponseEntity.ok(geminiResponse);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing audio file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating Gemini response: " + e.getMessage());
        }
    }
}