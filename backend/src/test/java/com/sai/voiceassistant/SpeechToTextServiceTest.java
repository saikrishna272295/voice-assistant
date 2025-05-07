package com.sai.voiceassistant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpeechToTextServiceTest {

    @InjectMocks
    private SpeechToTextService speechToTextService;

    @BeforeEach
    public void setUp() {
        // Set API key for testing
        ReflectionTestUtils.setField(speechToTextService, "googleApiKey", "test-api-key");
    }

    @Test
    public void testTranscribe_NullAudioData() {
        // Test with null audio data
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            speechToTextService.transcribe(null);
        });
        
        assertEquals("Audio data is empty", exception.getMessage());
    }

    @Test
    public void testTranscribe_EmptyAudioData() {
        // Test with empty audio data
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            speechToTextService.transcribe(new byte[0]);
        });
        
        assertEquals("Audio data is empty", exception.getMessage());
    }

    @Test
    public void testTranscribe_MissingApiKey() {
        // Set empty API key
        ReflectionTestUtils.setField(speechToTextService, "googleApiKey", "");
        
        // Test with valid audio data but missing API key
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            speechToTextService.transcribe("test audio".getBytes());
        });
        
        assertEquals("Google API key is not configured", exception.getMessage());
    }

    // Note: Testing the actual API call would require mocking the HTTP connection
    // This would be a more complex integration test that could be implemented
    // using tools like MockServer or WireMock to simulate Google's API responses
}