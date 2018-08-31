package com.tree.tatanpoker.audio;

import com.google.cloud.speech.v1.RecognitionConfig;
import javazoom.jl.decoder.JavaLayerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class HotwordRecognition extends Thread{
    public abstract void startRecognition() throws IOException;
    public abstract void stopRecognition();

    public void on_recognition(){
        try {
            File file = new File("src/main/resources/audio/detected.mp3");
            FileInputStream fis = new FileInputStream(file);
            System.out.println(file.getAbsolutePath());
            SpeechHandler.playAudio(fis);
            //TODO Google APIS.
            SpeechHandler.speechToText("src/main/resources/audio/test.mp3");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JavaLayerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
