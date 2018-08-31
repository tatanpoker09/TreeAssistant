package com.tree.tatanpoker.main;

import com.google.cloud.texttospeech.v1beta1.SynthesizeSpeechResponse;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import com.tree.tatanpoker.audio.CMUSphinxHandler;
import com.tree.tatanpoker.audio.HotwordRecognition;
import com.tree.tatanpoker.audio.PorcupineHandler;
import com.tree.tatanpoker.audio.SpeechHandler;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class TreeAssistant {
    private static final Logger log = Logger.getLogger("TreeAssistant");
    public static void main(String[] args){
        setupManagers();
    }

    private static void setupManagers() {
        SpeechHandler sh = new SpeechHandler();
        sh.speak("Hello, and welcome to Tree Voice Assistant.");
        HotwordRecognition handler = new PorcupineHandler();
        try {
            handler.startRecognition();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logInfo(String s) {
        log.info(s);
    }

    public static void logError(String s){
        log.severe(s);
    }


    private void record(){

        AudioFormat format = new AudioFormat(1024, 16, 1, true, true);
        TargetDataLine microphone = null;
        SourceDataLine speakers;
        System.out.println("Recording...");
        int bufferSize = 1024/2;
        try {
            microphone = AudioSystem.getTargetDataLine(format);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            int bytesRead = 0;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] data = new byte[bufferSize];
            microphone.open(format);
            microphone.start();
            ByteOutputStream outputStream = new ByteOutputStream();
            while(bytesRead<100000){ //Just so I can test if recording my mic works...
                int bytes = microphone.read(data, 0, data.length);
                if(bytes==data.length){
                    short[] shorts = new short[bufferSize];
                    for(int i = 0; i<512;i++) {
                        shorts[i] = data[i];
                    }
                    outputStream.write(data);
                    bytesRead++;
                } else {
                    log.info("Not enough samples for the audio consumer..."+bytes+","+data.length);
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } finally {
            if (microphone != null) {
                microphone.close();
                microphone.flush();
            }
        }
    }
}
