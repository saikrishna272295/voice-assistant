package com.sai.voiceassistant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AudioController.class)
public class AudioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpeechToTextService speechToTextService;

    @Test
    public void testTranscribeAudio_Success() throws Exception {
        // Prepare test data
        byte[] audioBytes = "test audio content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.wav",
                "audio/wav",
                audioBytes);
        
        // Mock service response
        when(speechToTextService.transcribe(any(byte[].class)))
                .thenReturn("This is the transcribed text");

        // Perform request and verify response
        mockMvc.perform(multipart("/api/audio/transcribe")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("This is the transcribed text"));
    }

    @Test
    public void testTranscribeAudio_EmptyFile() throws Exception {
        // Prepare empty file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.wav",
                "audio/wav",
                new byte[0]);

        // Perform request and verify response
        mockMvc.perform(multipart("/api/audio/transcribe")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No file uploaded"));
    }

    @Test
    public void testTranscribeAudio_ServiceError() throws Exception {
        // Prepare test data
        byte[] audioBytes = "test audio content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.wav",
                "audio/wav",
                audioBytes);
        
        // Mock service throwing exception
        when(speechToTextService.transcribe(any(byte[].class)))
                .thenThrow(new IOException("API error"));

        // Perform request and verify response
        mockMvc.perform(multipart("/api/audio/transcribe")
                .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error processing audio file: API error"));
    }
}