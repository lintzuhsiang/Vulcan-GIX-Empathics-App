
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Time;

import static java.lang.String.*;

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
    String currTime;
    private File rawfile;

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

        this.bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT)*5;
        Log.d("mic", "initMic");
        this.recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT, this.bufferSizeInBytes);
        this.recorder.startRecording();

    }

    public void startRecording(final String currTime) throws IOException {
        this.currTime = currTime;

        Log.d("mic",currTime);
        Log.d("mic", "saveRecording");
        rawfile = getFile("raw");

        System.out.println(rawfile.getClass());
        long audiostarttime = System.currentTimeMillis();
//        final byte data[] = new byte[this.bufferSizeInBytes];
//        this.isRecording = isRecording;
        short[] mBuffer = new short[this.bufferSizeInBytes];



        DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(rawfile)));

//        if (stream != null){
        while(System.currentTimeMillis() - audiostarttime < 1000){
//
            double sum = 0;
            int readSize = this.recorder.read(mBuffer, 0, mBuffer.length);
            for (int i = 0; i < readSize; i++) {
                stream.writeShort(mBuffer[i]);
                sum += mBuffer[i] * mBuffer[i];
            }
            if (readSize > 0) {
                final double amplitude = sum / readSize;
            }
        }
        try{
            saveRecording();
            stream.flush();
            stream.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        Log.d("mic","after runnable");
    }

    public void saveRecording(){
        File wavfile = getFile("wav");
        try {
            rawToWave(rawfile,wavfile);
            Log.d("file","save Recording");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording(){
        File wavfile = getFile("wav");
        try {
            rawToWave(rawfile,wavfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.isRecording = false;
        Log.d("mic", "stopRecording");
        Log.d("mic", valueOf(isRecording));

        recorder.stop();
        recorder.release();
        Log.d("mic","stop recording");
    }

    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }
        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, SAMPLE_RATE); // sample rate
            writeInt(output, SAMPLE_RATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }
            output.write(bytes.array());
        } finally {
            if (output != null) {
                output.close();
                rawFile.delete();
            }
        }

    }
    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }
    private File getFile(final String suffix) {
        return new File(this.currTime + "." + suffix);
    }
}