import React, { useState, useRef } from "react";
import "./App.css";

function App() {
  const [recording, setRecording] = useState(false);
  const [assistantResponse, setAssistantResponse] = useState("");
  const [error, setError] = useState("");
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);

  const startRecording = async () => {
    setError("");
    setAssistantResponse("");
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mediaRecorder = new window.MediaRecorder(stream);
      mediaRecorderRef.current = mediaRecorder;
      audioChunksRef.current = [];
      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };
      mediaRecorder.onstop = handleStop;
      mediaRecorder.start();
      setRecording(true);
    } catch (err) {
      setError("Microphone access denied or not available.");
    }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current) {
      mediaRecorderRef.current.stop();
      setRecording(false);
    }
  };

  const handleStop = async () => {
    const audioBlob = new Blob(audioChunksRef.current, { type: "audio/webm" });
    const wavBlob = await convertToWav(audioBlob);
    sendAudioToBackend(wavBlob);
  };

  // Convert WebM/Opus to WAV using an offscreen audio context
  const convertToWav = async (blob) => {
    const arrayBuffer = await blob.arrayBuffer();
    const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    const audioBuffer = await audioCtx.decodeAudioData(arrayBuffer);
    const wavBuffer = encodeWAV(audioBuffer);
    return new Blob([wavBuffer], { type: "audio/wav" });
  };

  // WAV encoding helper
  function encodeWAV(audioBuffer) {
    const numChannels = audioBuffer.numberOfChannels;
    const sampleRate = audioBuffer.sampleRate;
    const format = 1; // PCM
    const bitDepth = 16;
    const samples = audioBuffer.length * numChannels;
    const buffer = new ArrayBuffer(44 + samples * 2);
    const view = new DataView(buffer);
    // RIFF identifier
    writeString(view, 0, "RIFF");
    view.setUint32(4, 36 + samples * 2, true);
    writeString(view, 8, "WAVE");
    writeString(view, 12, "fmt ");
    view.setUint32(16, 16, true); // Subchunk1Size
    view.setUint16(20, format, true); // AudioFormat
    view.setUint16(22, numChannels, true);
    view.setUint32(24, sampleRate, true);
    view.setUint32(28, sampleRate * numChannels * 2, true); // ByteRate
    view.setUint16(32, numChannels * 2, true); // BlockAlign
    view.setUint16(34, bitDepth, true); // BitsPerSample
    writeString(view, 36, "data");
    view.setUint32(40, samples * 2, true);
    // Write PCM samples
    let offset = 44;
    for (let i = 0; i < audioBuffer.length; i++) {
      for (let ch = 0; ch < numChannels; ch++) {
        let sample = audioBuffer.getChannelData(ch)[i];
        sample = Math.max(-1, Math.min(1, sample));
        view.setInt16(offset, sample < 0 ? sample * 0x8000 : sample * 0x7FFF, true);
        offset += 2;
      }
    }
    return buffer;
  }
  function writeString(view, offset, string) {
    for (let i = 0; i < string.length; i++) {
      view.setUint8(offset + i, string.charCodeAt(i));
    }
  }

  const sendAudioToBackend = async (wavBlob) => {
    setAssistantResponse("Processing...");
    try {
      const formData = new FormData();
      formData.append("file", wavBlob, "recording.wav");
      const response = await fetch("http://localhost:8080/api/audio/transcribe", {
        method: "POST",
        body: formData,
      });
      if (!response.ok) throw new Error("Failed to get response from backend");
      let text = await response.text();
      try {
        const data = JSON.parse(text);
        setAssistantResponse(data.response || text || "No response from assistant.");
      } catch (e) {
        setAssistantResponse(text || "No response from assistant.");
      }
    } catch (err) {
      setAssistantResponse("");
      setError("Error communicating with backend: " + err.message);
    }
  };

  return (
    <div className="App">
      <header className="App-header">
        <h2>Voice Assistant</h2>
        <div style={{ margin: "20px" }}>
          <button onClick={startRecording} disabled={recording} style={{ marginRight: 10 }}>
            Start Recording
          </button>
          <button onClick={stopRecording} disabled={!recording}>
            Stop Recording
          </button>
        </div>
        {error && <div style={{ color: "#ff4d4f" }}>{error}</div>}
        <div style={{ marginTop: 20, minHeight: 40 }}>
          <strong>Assistant:</strong>
          <div style={{
            display: "inline-block",
            marginLeft: 8,
            padding: "10px 14px",
            border: "1px solid #b5e853",
            borderRadius: 6,
            background: "#181c20",
            color: "#b5e853",
            width: 454,
            height: 454,
            maxWidth: 454,
            maxHeight: 454,
            minWidth: 454,
            minHeight: 454,
            overflowY: "auto",
            verticalAlign: "top",
            fontSize: 16,
            textAlign: "left"
          }}>
            {assistantResponse}
          </div>
        </div>
      </header>
    </div>
  );
}

export default App;
