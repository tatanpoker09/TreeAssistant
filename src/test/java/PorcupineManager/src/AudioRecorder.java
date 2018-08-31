/*
 * Copyright 2018 Picovoice Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;


/**
 * Record the audio data from microphone and pass the raw PCM data to {@link AudioConsumer}.
 */
public class AudioRecorder {


    private static final String TAG = AudioRecorder.class.getName();
    private static final Logger log = Logger.getLogger(TAG);

    private final AudioConsumer audioConsumer;
    private final int sampleRate;
    private final int frameLength;

    private AtomicBoolean started = new AtomicBoolean(false);
    private AtomicBoolean stop = new AtomicBoolean(false);
    private AtomicBoolean stopped = new AtomicBoolean(false);

    /**
     * A task to record audio and send the audio samples to ai.picovoice.porcupine.Porcupine library for processing.
     */
    private class RecordTask implements Callable<Void> {
        /**
         * Record audio.
         * @return return null that is needed by the {@link Callable} interface.
         * @throws PorcupineManagerException An exception is thrown if {@link AudioRecord} or
         * {@link ai.picovoice.porcupine} throws an error.
         */
        @Override
        public Void call() throws PorcupineManagerException {
            // Set the priority of this thread.

            record();
            return null;
        }
    }

    /**
     * Initialize AudioRecorder.
     * @param audioConsumer Consumer for the audio samples recorded by {@link AudioRecorder}.
     */
    AudioRecorder(AudioConsumer audioConsumer) {
        this.audioConsumer = audioConsumer;
        this.sampleRate = audioConsumer.getSampleRate();
        this.frameLength = audioConsumer.getFrameLength();
    }

    /**
     * Start recording in a worker thread.
     * @throws PorcupineManagerException exception is thrown if the {@link RecordTask} throws an error.
     */
    void start() throws PorcupineManagerException {
        if (started.get()) {
            return;
        }
        started.set(true);
        RecordTask recordTask = new RecordTask();
        ExecutorService recordExecutor = Executors.newSingleThreadExecutor();
        recordExecutor.submit(recordTask);
    }

    /**
     * Stop the recorder gracefully.
     * @throws InterruptedException if the thread is interrupted.
     */
    void stop() throws InterruptedException{
        if (!started.get()) {
            return;
        }
        stop.set(true);
        while (!stopped.get()) {
            Thread.sleep(10);
        }
        started.set(false);
    }

    /***
     * Record the audio and call the {@link AudioConsumer} to consume the raw PCM data.
     * @throws PorcupineManagerException exception is thrown if {@link AudioConsumer} throws an error or
     * {@link AudioRecord} throws an error.
     */
    private void record() throws PorcupineManagerException {

        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, true);
        TargetDataLine microphone = null;
        SourceDataLine speakers;
        System.out.println("Recording...");
        int bufferSize = sampleRate/2;
        try {
            microphone = AudioSystem.getTargetDataLine(format);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            int bytesRead = 0;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] data = new byte[frameLength];
            microphone.open(format);
            microphone.start();
            ByteOutputStream outputStream = new ByteOutputStream();
            while(bytesRead<100000){ //Just so I can test if recording my mic works...
                int bytes = microphone.read(data, 0, data.length);
                if(bytes==data.length){
                    short[] shorts = new short[frameLength];
                    for(int i = 0; i<512;i++) {
                        shorts[i] = data[i];
                    }
                    outputStream.write(data);
                    audioConsumer.consume(shorts);
                    bytesRead++;
                } else {
                    log.info("Not enough samples for the audio consumer..."+bytes+","+data.length);
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new PorcupineManagerException(e);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } finally {
            if (microphone != null) {
                microphone.close();
                microphone.flush();
            }
            stopped.set(true);
        }
    }
}