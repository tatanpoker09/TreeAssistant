package com.tree.tatanpoker.audio;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.cloud.speech.v1.*;
import com.google.cloud.texttospeech.v1beta1.*;
import com.google.protobuf.ByteString;
import com.tree.tatanpoker.config.ConfigLoader;
import com.tree.tatanpoker.main.TreeAssistant;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import jdk.internal.util.xml.impl.Input;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SpeechHandler implements ConfigLoader {
    private static final AudioEncoding ENCODING = AudioEncoding.MP3;

    private static String LANGUAGE = "en-US"; //Defaults to english.
    private static SsmlVoiceGender GENDER = SsmlVoiceGender.FEMALE;


    private SynthesizeSpeechResponse synthesize(String text) throws Exception {
        try (TextToSpeechClient client = TextToSpeechClient.create()) {
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();
            VoiceSelectionParams params = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(LANGUAGE)
                    .setSsmlGender(GENDER)
                    .build();

            AudioConfig config = AudioConfig.newBuilder()
                    .setAudioEncoding(ENCODING)
                    .build();

            SynthesizeSpeechResponse response = client.synthesizeSpeech(input, params, config);
            return response;
        }
    }

    private void playAudio(ByteString audio) throws JavaLayerException {
        playAudio(audio.newInput());
    }

    public static void playAudio(InputStream stream) throws JavaLayerException {
        Player player = new Player(stream);
        player.play();
    }

    public void speak(String text) {
        SynthesizeSpeechResponse response = null;
        try {
            response = synthesize(text);
            playAudio(response.getAudioContent());
        } catch (JavaLayerException e) {
            TreeAssistant.logError("Error while playing response audio");
        } catch (Exception e) {
            TreeAssistant.logError("Error while connecting with Google APIs.");
            e.printStackTrace();
        }

    }

    public static String getCurrentLanguage() {
        return LANGUAGE;
    }

    public static void setLanguages(String language) {
        SpeechHandler.LANGUAGE = LANGUAGE;
    }

    @Override
    public void loadConfig(YamlReader reader) {

    }
    public static byte[] inputStreamToByteArray(InputStream inStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inStream.read(buffer)) > 0) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    public static void speechToText(String fileName) throws IOException {
        if (fileName == null) return;
        try (SpeechClient speechClient = SpeechClient.create()) {
            byte[] data = inputStreamToByteArray(new FileInputStream(fileName));
            ByteString audioBytes = ByteString.copyFrom(data);

            // Builds the sync recognize request
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding.LINEAR16)
                    .setSampleRateHertz(16000)
                    .setLanguageCode("es-ES")
                    .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            // Performs speech recognition on the audio file
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcription: %s%n", alternative.getTranscript());
            }
        }
    }
}