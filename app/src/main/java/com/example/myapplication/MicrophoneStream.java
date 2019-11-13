//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//
package com.example.myapplication;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * MicrophoneStream exposes the Android Microphone as an PullAudioInputStreamCallback
 * to be consumed by the Speech SDK.
 * It configures the microphone with 16 kHz sample rate, 16 bit samples, mono (single-channel).
 */
public class MicrophoneStream extends PullAudioInputStreamCallback {
    private final static int SAMPLE_RATE = 16000;
//    private final AudioStreamFormat format;
    private AudioRecord recorder;
    static public int  bufferSizeInBytes;
    boolean isRecording;

    public MicrophoneStream() {
//        this.format = AudioStreamFormat.getWaveFormatPCM(SAMPLE_RATE, (short)16, (short)1);
        this.initMic();
    }

    @Override
    public int read(byte[] bytes) {
        long ret = this.recorder.read(bytes, 0, bytes.length);
        return (int)ret;
    }

    @Override
    public void close() {
        this.recorder.release();
        this.recorder = null;
    }

    private void initMic() {

        this.bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        Log.d("mic", "initMic");
        this.recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT, this.bufferSizeInBytes);
        this.recorder.startRecording();

    }

    public void saveRecording(boolean isRecording,final String currTime){
        Log.d("mic",currTime);
        Log.d("mic", "saveRecording");
        Log.d("mic", String.valueOf(isRecording));
        long audiotime = System.currentTimeMillis();
        final byte data[] = new byte[this.bufferSizeInBytes];
        this.isRecording = isRecording;


        FileOutputStream stream = null;
        try{
            stream = new FileOutputStream(currTime);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

        if (stream != null){
            while(this.isRecording){
                int read = recorder.read(data, 0, this.bufferSizeInBytes);
                Log.d("read", String.valueOf(read));
                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try{
                        stream.write(data);
                        Log.d("mic","save audio");
                    }catch (IOException e){
                        e.printStackTrace();
                        Log.d("mic","save error");
                    }
                }
            }
    try{
        stream.close();
    }catch(IOException e){
        e.printStackTrace();
    }
        }
         Log.d("mic","after runnable");
    }

    public void stopRecording(){
        this.isRecording = false;
        Log.d("mic", "stopRecording");
        Log.d("mic", String.valueOf(isRecording));

        recorder.stop();
        recorder.release();
        Log.d("mic","stop recording");
    }
}
