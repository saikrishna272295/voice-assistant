# Voice Assistant Backend Testing Guide

This guide provides instructions on how to test the Voice Assistant backend application.

## Prerequisites

- Java 17 or higher
- Maven
- Google Cloud Speech-to-Text API key

## Setting Up for Testing

1. Configure your Google API key in `application.properties`:
   ```
   google.api.key=YOUR_GOOGLE_API_KEY
   ```

2. Make sure all dependencies are installed:
   ```
   mvn clean install
   ```

## Running the Tests

### Running All Tests

To run all tests including unit tests and integration tests:

```
mvn test
```

### Running Specific Test Classes

To run a specific test class:

```
mvn test -Dtest=AudioControllerTest
```

or

```
mvn test -Dtest=SpeechToTextServiceTest
```

## Test Coverage

The test suite includes:

1. **Unit Tests**:
   - `AudioControllerTest`: Tests the REST controller endpoints
   - `SpeechToTextServiceTest`: Tests the speech-to-text service functionality

2. **Integration Tests**:
   - `VoiceAssistanceBackendApplicationTests`: Verifies the Spring Boot application context loads correctly

## Manual Testing

You can also test the API manually using tools like Postman or curl:

```bash
curl -X POST \
  http://localhost:8080/api/audio/transcribe \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@/path/to/your/audio/file.wav'
```

Or using Postman:
1. Create a new POST request to `http://localhost:8080/api/audio/transcribe`
2. In the Body tab, select form-data
3. Add a key named "file" of type File and select your audio file
4. Send the request

## Troubleshooting

- If tests fail with API key errors, ensure your Google API key is correctly set in `application.properties`
- For HTTP connection issues, check your internet connection and firewall settings
- If you encounter "Audio data is empty" errors, ensure your test audio files are valid

## Notes on Google Speech-to-Text API

- The free tier of Google Speech-to-Text API has usage limits
- For testing purposes, consider using mock responses instead of making actual API calls
- The API expects audio in LINEAR16 format with a sample rate of 16000Hz