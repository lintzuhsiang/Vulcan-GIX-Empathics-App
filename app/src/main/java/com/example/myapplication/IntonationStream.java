package com.example.myapplication;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.microsoft.cognitiveservices.speech.audio.AudioOutputStream;
import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback;
import com.microsoft.cognitiveservices.speech.audio.PullAudioOutputStream;
//import com.microsoft.cognitiveservices.speech.internal.PullAudioOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.*;
//public class IntonationStream extends AudioOutputStream{

public class IntonationStream extends AudioOutputStream{
    private static com.microsoft.cognitiveservices.speech.internal.AudioOutputStream audioOutputStream;

    private static String TAG = "Empethics/Intonation";
    static public int bufferSizeInBytes;
    private final static int SAMPLE_RATE = 16000;
    private String currTime;
    private AudioRecord recorder;
    private File rawfile;
    private boolean isRecord = false;

    protected IntonationStream(com.microsoft.cognitiveservices.speech.internal.AudioOutputStream audioOutputStream) {
        super(audioOutputStream);
        initRecord();
    }

    public IntonationStream(){
        super(audioOutputStream);
    }

    private byte[] bBuffer = new byte[this.bufferSizeInBytes];




    private PullAudioOutputStream Stream = IntonationStream.createPullStream();



    public void initRecord(){
        this.bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 20;

        this.recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, this.bufferSizeInBytes);
        this.recorder.startRecording();
    }

    public void StartRecording(String currTime) throws IOException {
        Log.d(TAG,"startRecording Intonation");
        this.currTime = currTime;
        short[] mBuffer = new short[this.bufferSizeInBytes];
        rawfile = getFile("raw");
        long audioStartTime = System.currentTimeMillis();

        DataOutputStream stream = new DataOutputStream((new BufferedOutputStream(new FileOutputStream(rawfile))));

        while(System.currentTimeMillis() - audioStartTime < 1000){
            Stream.read(bBuffer);
//            int readSize = this.recorder.read(mBuffer,0,mBuffer.length);
            stream.write(bBuffer,0,bBuffer.length);
        }


//        while (System.currentTimeMillis() - audioStartTime < 1000) {
////
//            int readSize = this.recorder.read(mBuffer, 0, mBuffer.length);
//            for (int i = 0; i < readSize; i++) {
//                stream.writeShort(mBuffer[i]);
//            }
//        }
        try {
            saveRecording();
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void saveRecording() {
        File wavFile = getFile("wav");
        if (rawfile != null) {
            try {
                rawToWave(rawfile, wavFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void stopRecording(){
        File wavFile = getFile("wav");
        if(rawfile!=null){
            try{
                rawToWave(rawfile,wavFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.recorder.stop();
        this.recorder.release();

        this.isRecord = false;


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
    private File getFile(String suffix){
        return new File(this.currTime + "."+suffix);
    }


}
