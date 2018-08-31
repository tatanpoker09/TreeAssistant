package com.tree.tatanpoker.audio;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import javazoom.jl.decoder.JavaLayerException;

import java.io.*;

public class CMUSphinxHandler extends HotwordRecognition {
    private static final String HOTWORD = "alexa";
    private LiveSpeechRecognizer recognizer;

    @Override
    public void startRecognition() throws IOException {
        Configuration configuration = new Configuration();

        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setGrammarPath("src/main/resources/grammars");
        configuration.setGrammarName("hotword");
        configuration.setUseGrammar(true);
        recognizer = new LiveSpeechRecognizer(configuration);
        recognizer.startRecognition(true);
        recognize();
    }

    private void recognize() {
        SpeechResult result;
        while ((result = recognizer.getResult()) != null ) {
            if(result.getHypothesis().contains(HOTWORD)){
                on_recognition();
            }
        }
    }

    @Override
    public  void stopRecognition() {
        recognizer.stopRecognition();
    }
}
