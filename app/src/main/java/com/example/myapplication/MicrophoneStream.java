
//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//
package com.example.myapplication;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static java.lang.String.*;

/**
 * MicrophoneStream exposes the Android Microphone as an PullAudioInputStreamCallback
 * to be consumed by the Speech SDK.
 * It configures the microphone with 16 kHz sample rate, 16 bit samples, mono (single-channel).
 */
public class MicrophoneStream extends PullAudioInputStreamCallback {
    private final static int SAMPLE_RATE = 16000;
    private static final String TAG = "Empethics/Microphone";
    private AudioRecord recorder;
    static public int bufferSizeInBytes;
    boolean isRecording;
    Date now = new Date();
    private String fileDir = "/storage/emulated/0/Android/data/com.example.myapplication/files/Pictures/";
    private File rawfile = getFile(fileDir + now, ".raw");
    DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(rawfile)));
    byte[] buffer;
    short[] mbuffer;

    public SimpleDateFormat DateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);

    public MicrophoneStream() throws FileNotFoundException {
        this.initMic();
    }

    @Override
    public int read(byte[] bytes) {
        long ret = this.recorder.read(bytes, 0, bytes.length);
        Log.d("tmp", String.valueOf(ret));

//        byte[] buffer = Arrays.copyOf(bytes,bytes.length);
//        short[] mbuffer = Arrays.copyOf(bytes,bytes.length);
//        buffer = new byte[bytes.length];
        for(int i=0;i<bytes.length;i++){
            buffer[i] = bytes[i];
//            mbuffer[i] = (short) bytes[i];
            try {
//                stream.writeShort(mbuffer[i]);
                stream.write(buffer[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return (int) ret;
    }

    final Handler handler = new Handler();

    public void record() throws IOException {
        now = new Date();
        rawfile = getFile(fileDir + formatter.format(now), "raw");
        stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(rawfile)));
        Log.d("tmp", "tmp");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("tmp", "tmp2");
                    Log.d("tmp", rawfile.getAbsolutePath());
                    stream.flush();
                    saveRecording(rawfile);
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 2000);
    }

    @Override
    public void close() {
        this.recorder.release();
        this.recorder = null;
    }

    public interface ResponseListener{
        void onComplete(boolean flag);
    }
    public ResponseListener audioListener;

    public void setAudioListener(ResponseListener responseListener){
        audioListener = responseListener;
    }
    private void initMic() {

        this.bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 20;
        Log.d(TAG, "initMic");
        Log.d(TAG, String.valueOf(this.bufferSizeInBytes));
        mbuffer = new short[bufferSizeInBytes];
        buffer = new byte[bufferSizeInBytes];
        this.recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, this.bufferSizeInBytes);
        this.recorder.startRecording();


    }

    public void StartRecording(final String currTime) throws IOException {
//        this.currTime = currTime;
        Log.d(TAG, "StartRecording: buffer length  "+buffer.length);

        Log.d(TAG, currTime);
        rawfile = getFile(fileDir + formatter.format(now), "raw");

        long audiostarttime = System.currentTimeMillis();
        byte[] bBuffer = new byte[this.bufferSizeInBytes];
        short[] mBuffer = new short[this.bufferSizeInBytes];

        DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(rawfile)));

        while (System.currentTimeMillis() - audiostarttime < 1000) {
//
            int readSize = this.recorder.read(mBuffer, 0, mBuffer.length);
            Log.d(TAG,"readSize "+readSize);
            for (int i = 0; i < readSize; i++) {
                stream.writeShort(mBuffer[i]);
            }
//            int readSize = this.read(buffer);
//            for(int i=0;i<readSize;i++){
//                stream.write(buffer[i]);
//            }
        }
        try {
            saveRecording(rawfile);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveRecording(File rawfile) {
        File wavfile = getFile(fileDir + formatter.format(now), "wav");
        Log.d(TAG,wavfile.getAbsolutePath());
        if (rawfile != null) {
            try {
                rawToWave(rawfile, wavfile);
                Log.d(TAG, "save Recording");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(audioListener!=null){
                    audioListener.onComplete(true);
                    Log.d(TAG, "save Recording2");

                }
            }
        }
    }

    public void stopRecording() {
        now = new Date();
        File wavfile = getFile(fileDir + formatter.format(now), "wav");

        if (rawfile != null) {
            try {
                rawToWave(rawfile, wavfile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.isRecording = false;

        recorder.stop();
        recorder.release();
        Log.d(TAG, "stop recording");
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
                Log.d(TAG, "save Recording wav");

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

    private File getFile(final String filepath, final String suffix) {
        return new File(filepath +"_read_buffer"+ "." + suffix);
    }
}