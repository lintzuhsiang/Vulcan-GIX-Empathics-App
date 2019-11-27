package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.hardware.camera2.*;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.microsoft.cognitiveservices.speech.audio.AudioOutputStream;
import com.microsoft.cognitiveservices.speech.audio.AudioInputStream;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionEventArgs;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.PullAudioOutputStream;
import com.microsoft.cognitiveservices.speech.internal.AudioDataStream;
import com.microsoft.cognitiveservices.speech.internal.PullAudioInputStreamCallback;
import com.microsoft.cognitiveservices.speech.util.EventHandler;
import com.vuzix.hud.actionmenu.ActionMenuActivity;

import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;
import com.microsoft.cognitive.textanalytics.retrofit.ServiceCall;
import com.microsoft.cognitive.textanalytics.retrofit.ServiceCallback;
import com.microsoft.cognitive.textanalytics.retrofit.ServiceRequestClient;

import org.json.JSONException;
import org.json.JSONObject;

//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.FileBody;
//import org.apache.http.entity.mime.content.StringBody;


public class MainActivity extends ActionMenuActivity {
//public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Empethics";

    //layout
    private Button takePictureButton;
    private TextureView textureView;
    private ImageView imageView;
    private ImageView imageViewRedDot;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    //camera
    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Bitmap storedBitmap;
    private File file;

    private ImageReader reader;
    //thread
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private Handler micBackgroundHandler;
    private HandlerThread micBackgroundThread;

    private Handler timerHandler = new Handler();
    private Runnable runnableCode;
    private Handler micHandler = new Handler();
    private Runnable micRunnable;

//    private AudioTry Audiotry = new AudioTry();


    //timestamp
    long appstartTime = SystemClock.elapsedRealtime();
    protected long faceAPIStartTime = 0;

    //face API
    String FACE_SUBSCRIPTION_KEY = "bc027dc227484433a77d7b613807d230";
    String FACE_ENDPOINT = "https://empthetic.cognitiveservices.azure.com/face/v1.0";
    private final String apiEndpoint = FACE_ENDPOINT;
    private final String subscriptionKey = FACE_SUBSCRIPTION_KEY;
    private final FaceServiceClient faceServiceClient = new FaceServiceRestClient(apiEndpoint, subscriptionKey);
    public String fileDir;
    public SimpleDateFormat DateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());


    //Speech-to-Text and Text Sentiment API
    private static final String SpeechSubscriptionKey = "d122e91d2df24ce889a13695542564c2";
//    private static final String SpeechSubscriptionKey = "e7dc5968d6df46c78fea91b55c51c8a7";
    private static final String SpeechRegion = "eastus";
    private ServiceCall mSentimentCall;
    private String preSpeechResult;

    private SpeechConfig speechConfig;
    private String sentimentResult = "";

    byte[] bBuffer = new byte[10000];
    NetworkClient Client = new NetworkClient();
    Sentiment sentiment = new Sentiment();
    boolean continuousListeningStarted = false;
    SpeechRecognizer reco = null;


    private Integer AsequenceID = 0;
    static public Integer IsequenceID = 0;
    static public Integer SsequenceID = 0;
    static public Integer MsequenceID = 0;
    public ArrayList<String> score_array = new ArrayList<String>(){{add("0.5");add("0.5");}};
    private String android_id;
    User user = new User();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureView = findViewById(R.id.textureView);
        assert textureView != null;
//        textureView.setSurfaceTextureListener(textureListener);
        takePictureButton = findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;

        fileDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        imageView = findViewById(R.id.imageView);
        imageViewRedDot = findViewById(R.id.imageView2);

        Toast.makeText(MainActivity.this, "Tap to start.", Toast.LENGTH_LONG).show();

        android_id = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        user.setDeviceId(android_id);
        Log.d(TAG, user.getDeviceId());


        try {
            speechConfig = SpeechConfig.fromSubscription(SpeechSubscriptionKey, SpeechRegion);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return;
        }
        Client.getSessionID();

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (continuousListeningStarted) {
                    Log.d(TAG, "continuousListeningStarted True");
                    Log.d(TAG, String.valueOf(continuousListeningStarted));
                    if (reco != null) {
                        final Future<Void> task = reco.stopContinuousRecognitionAsync();

//                        microphoneStream.stopRecording();
                        setSSTCompletedListener(task, new OnTaskCompletedListener<Void>() {
                            @Override
                            public void onCompleted(Void result) {
                                Log.i(TAG, "Continuous recognition stopped.");
                            }
                        });
                        continuousListeningStarted = false;
                    } else {
                        continuousListeningStarted = false;
                    }


                    Toast.makeText(MainActivity.this, "Emotion detection: Off", Toast.LENGTH_SHORT).show();
//                    timerHandler.removeCallbacks(runnableCode);

                    int imageResource = getResources().getIdentifier("@drawable/pause", "drawable", getPackageName());
                    imageViewRedDot.setImageResource(imageResource);
                    imageView.setVisibility(View.INVISIBLE);

                    Log.d(TAG, "Stop runnable on main thread");

                } else {
                    Log.d(TAG, "continuousListeningStarted False");
                    Log.d(TAG, String.valueOf(continuousListeningStarted));
                    int imageResource = getResources().getIdentifier("@drawable/reddot", "drawable", getPackageName());
                    imageViewRedDot.setImageResource(imageResource);

                    Toast.makeText(MainActivity.this, "Emotion detection: On", Toast.LENGTH_SHORT).show();
                    imageViewRedDot.setVisibility(View.VISIBLE);

                    SpeechSentimentInit();
                    UploadToServer();
                }
            }
        });
    }

//    private IntonationStream intonationStream;

    private MicrophoneStream microphoneStream;

//    private IntonationStream createPullStream() {
//        if (intonationStream != null) {
//            intonationStream.close();
//            intonationStream = null;
//        }
//        intonationStream = new IntonationStream();
//        return intonationStream;
//    }

    private MicrophoneStream createMicrophoneStream() throws FileNotFoundException {
        if (microphoneStream != null) {
            microphoneStream.close();
            microphoneStream = null;
        }

        microphoneStream = new MicrophoneStream();
        return microphoneStream;
    }


//    private void audioOutput() {
//        Integer bufferSizeInBytes = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 20;
//        byte[] bBuffer = new byte[bufferSizeInBytes];
//
////        try {
////            intonationStream.StartRecording("here");
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//        PullAudioOutputStream stream = AudioOutputStream.createPullStream();
//
////        long audioStartTime = System.currentTimeMillis();
////        stream.read(bBuffer);
//        Date now = new Date();
//        try {
//            intonationStream.StartRecording(String.valueOf(now));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
////        AudioConfig streamConfig = AudioConfig.fromStreamOutput(stream);
//    }

    private void SpeechSentimentInit() {
        Log.d(TAG, "SpeechSentimentInit");
        AudioConfig audioInput;

        try {
            audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
//            microphoneStream.read()
            reco = new SpeechRecognizer(speechConfig, audioInput);

            reco.recognized.addEventListener(new EventHandler<SpeechRecognitionEventArgs>() {
                @Override
                public void onEvent(Object o, SpeechRecognitionEventArgs speechRecognitionResultEventArgs) {
                    final String s = speechRecognitionResultEventArgs.getResult().getText();
                    Log.i(TAG, "Final result received: " + s);
                    if (s.length() > 1) {
                        sentiment.afterTextchange(s);
                        sentimentResult = sentiment.getSentimentScore();
                    }
                }
            });
            continuousListeningStarted = true;
//
            final Future<Void> task = reco.startContinuousRecognitionAsync();
            setSSTCompletedListener(task, new OnTaskCompletedListener<Void>() {
                @Override
                public void onCompleted(Void result) {
                    continuousListeningStarted = true;
                }
            });
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        Log.d(TAG, "SpeechSentimentInit ends");

    }

    private void setResultOnGlass() {
        Client.setResultListener(new NetworkClient.ResultListener() {
            @Override
            public void onComplete(String result) {
//                Log.d(TAG,"onComplete "+result);
                int imageResource = 0;
                switch (result) {
                    default:
                        imageResource = android.R.color.transparent;
                        break;
                    case "0":
                        imageResource = getResources().getIdentifier("@drawable/neutral", "drawable", getPackageName());
                        break;

                    case "2":
                        imageResource = getResources().getIdentifier("@drawable/happy", "drawable", getPackageName());
                        break;

                    case "1":
                        imageResource = getResources().getIdentifier("@drawable/sad", "drawable", getPackageName());
                        break;
                }
                imageView.setImageResource(imageResource);
                imageView.setVisibility(View.VISIBLE);
            }
        });
    }

    private Thread recordingThread;

    //Upload post_pic, post_senti_score, post_audio to web server periodically
    void UploadToServer() {
        ss_executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "UploadToServer");
                if (continuousListeningStarted) {
//                    MsequenceID = Math.min(Math.min(IsequenceID,AsequenceID),SsequenceID);
                    Log.d(TAG, "IsequenceID");
                    Log.d(TAG, String.valueOf(IsequenceID + 1));
                    Log.d(TAG, "SsequenceID");
                    Log.d(TAG, String.valueOf(SsequenceID + 1));
                    Log.d(TAG, "AsequenceID");
                    Log.d(TAG, String.valueOf(AsequenceID + 1));
                    Log.d(TAG, "MsequenceID");
                    Log.d(TAG, String.valueOf(MsequenceID + 1));
//                    sleep
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

                    //upload score 0.5 if not getting result from speech-to-text API
                    //update score from TextListener and update the score to cloud
                    SsequenceID += 1;
                    Log.d(TAG,"score_array: "+score_array);
//                    Client.uploadScore(String.valueOf(SsequenceID), "0.5", "0");
                    Client.uploadScore(String.valueOf(SsequenceID),score_array.get(score_array.size()-1));
                    score_array.add("0.5");
                    score_array.remove(0);
                    setTextListener(new TextChangeListener() {
                        @Override
                        public void onChanged(Object taskResult) {
//                            Log.d(TAG, String.valueOf(taskResult));
                            score_array.add(String.valueOf(taskResult));
                            score_array.remove(0);
//                            Client.uploadScore(String.valueOf(SsequenceID), (String) taskResult);
//                            Client.uploadML(String.valueOf(MsequenceID));
//
                        }
                    });

                    //take picture and send to cloud
                    takePicture();
                    MsequenceID += 1;
                    Client.uploadML = false;
                    Client.setuploadMLListener(new NetworkClient.ResponseListener() {
                        @Override
                        public void onComplete(boolean result) {
                            Log.d(TAG, "MLListener complete");
                            Client.uploadML(String.valueOf(MsequenceID));
                        }
                    });
                    //get Result from ML model and show on the glasses
                    setResultOnGlass();

                    //sleep
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //File path
                    fileDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

                    //microphone streaming recording and upload audio to server
                    Log.d(TAG, "uploadAudioandScore microphoneStream");
                    try {
                        microphoneStream.record();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    AsequenceID += 1;
                    microphoneStream.setAudioListener(new MicrophoneStream.ResponseListener() {
                        @Override
                        public void onComplete(boolean flag) {
//                            Client.uploadAudio(String.valueOf(AsequenceID), new File(fileDir + "/" + formatter.format(now) + ".wav"));
                        }
                    });

//
                } else {

                }
            }
        }, 0, 2000, TimeUnit.MILLISECONDS);
    }

    private static ExecutorService executorService;

    static {
        executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });
    }


    private <T> void setSSTCompletedListener(final Future<T> task, final OnTaskCompletedListener<T> listener) {
        ms_executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                T result = task.get();
                listener.onCompleted(result);
                return null;
            }
        });
    }

    private interface OnTaskCompletedListener<T> {
        void onCompleted(T taskResult);
    }

    private static ExecutorService ms_executorService;

    static {
        ms_executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });
    }

    private static ScheduledExecutorService ss_executorService;

    static {
        ss_executorService = Executors.newScheduledThreadPool(3, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });
    }


    static public TextChangeListener scorelistener;

    private void setTextListener(TextChangeListener listener) {
        scorelistener = listener;
    }

    public interface TextChangeListener<T> {
        void onChanged(T result);
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
//            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };


    private void takePicture() {
        if (null == cameraDevice) {
            openCamera();
            Log.e(TAG, "cameraDevice is null");
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            float maxzoom = (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)) * 10;

            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);

            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
//            captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            Date now = new Date();
            file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + formatter.format(now) + ".jpg");
//            file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + "picture" + ".jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();

                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        faceAPIStartTime = System.currentTimeMillis();

                        if (isExternalStorageWritable()) {
                            save(bytes);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                //save image to device
                private void save(byte[] bytes) throws IOException {
                    FileOutputStream output = null;
                    storedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                    Matrix mat = new Matrix();
                    mat.postRotate(270);
                    storedBitmap = Bitmap.createBitmap(storedBitmap, 0, 0, storedBitmap.getWidth(), storedBitmap.getHeight(), mat, true);
                    try {
                        output = new FileOutputStream(file);
                        storedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
//                    detectAndFrame(storedBitmap);
                    } finally {
                        if (null != output) {
                            Log.d(TAG, "image path: " + file.getAbsolutePath());
                            //sleep
//                            try {
////                                Thread.sleep(100);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                            IsequenceID += 1;
                            Client.uploadImage2(String.valueOf(IsequenceID), file);
//                            detectAndFrame(storedBitmap);
                            output.flush();
                            output.close();
                        }
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.d(TAG, String.format("takePicture end %s", appstartTime - SystemClock.elapsedRealtime()));
                    appstartTime = SystemClock.elapsedRealtime();
//                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.d(TAG, "CaptureSession Fail");
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != reader) {
            reader.close();
            reader = null;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(MainActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onDestroy() {
        closeCamera();
//        ss_executorService.shutdown();
//        ms_executorService.shutdown();
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        stopBackgroundThread();
        super.onPause();
    }


    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //    // detect capture frame and call Face API
    private void detectAndFrame(final Bitmap bitmap) {


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());

        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    //null          // returnFaceAttributes:
                                    new FaceServiceClient.FaceAttributeType[]{
                                            FaceServiceClient.FaceAttributeType.Age,
                                            FaceServiceClient.FaceAttributeType.Emotion
                                    }
                            );
                            if (result == null) {
                                publishProgress(
                                        "Detection Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(String.format(
                                    "Detection Finished. %d face(s) detected",
                                    result.length));
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Detection failed: %s", e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
//                        detectionProgressDialog.show();
                    }

                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
//                        detectionProgressDialog.setMessage(progress[0]);
                    }

                    @Override
                    protected void onPostExecute(Face[] result) {
                        //TODO: update face frames
//                        detectionProgressDialog.dismiss();

                        if (!exceptionMessage.equals("")) {
//                            showError(exceptionMessage);
                        }
                        if (result == null) return;
//
                        Log.d("emotion", String.format("detect frame ends %s", faceAPIStartTime - System.currentTimeMillis()));
                        imageView.setImageResource(showFaceResult(result));//,imageResource));
                        imageView.setVisibility(View.VISIBLE);
                    }
                };

        detectTask.execute(inputStream);
    }

    // get Face Result and return responding icon
    private int showFaceResult(Face[] faces) {
        Log.d("emotion", "Inside showFaceResult function");
        int imageResource = 0;

        String emo = "";
        if (faces != null) {
            Log.d("emotion", "face numbers %s" + faces.length);

            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
//                System.out.print(face.faceAttributes.emotion);
                Emotion faceEmotion = face.faceAttributes.emotion;
                double anger = faceEmotion.anger;
                double contempt = faceEmotion.contempt;
                double disgust = faceEmotion.disgust;
                double fear = faceEmotion.fear;
                double happiness = faceEmotion.happiness;
                double neutral = faceEmotion.neutral;
                double sadness = faceEmotion.sadness;
                double surprise = faceEmotion.surprise;
                double[] emoArrVal = {anger, contempt, disgust, fear, happiness, neutral, sadness, surprise};
                String[] emoArr = {"anger", "contempt", "disgust", "fear", "happiness", "neutral", "sadness", "surprise"};
                int maxEmo = getMaxValue(emoArrVal);
                emo = emoArr[maxEmo];

            }
        } else {
//            paint.setTextSize(400);
//            canvas.drawText("no face detected",75,385,paint);
//            canvas.translate(0,200);
        }
        Log.d("emotion", emo);

        switch (emo) {
            default:
                imageResource = android.R.color.transparent;
                break;
            case "neutral":
                imageResource = getResources().getIdentifier("@drawable/neutral", "drawable", getPackageName());
                break;

            case "happiness":
                imageResource = getResources().getIdentifier("@drawable/happy", "drawable", getPackageName());
                break;

            case "sadness":
                imageResource = getResources().getIdentifier("@drawable/sad", "drawable", getPackageName());
                break;
        }
        Log.d("imageResource", String.valueOf(imageResource));
        return imageResource;
    }


    public static int getMaxValue(double[] numbers) {
        double maxValue = numbers[0];
        int maxIndex = 0;

        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] > maxValue) {
                maxValue = numbers[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private boolean isExternalStorageWritable() {

        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());

    }

    protected void createCameraPreview() {
        try {
            int width = 640;
            int height = 480;
            //zoom in and out setting
//            float maxzoom = 10;
//            int zoom_level = 5;  //need to set finger event
//            int minW = (int) (width / maxzoom);
//            int minH = (int) (height / maxzoom);
//            int difW = width - minW;
//            int difH = height - minH;
//            int cropW = difW /100 *(int)zoom_level;
//            int cropH = difH /100 *(int)zoom_level;
//
//            cropW -= cropW & 3;
//            cropH -= cropH & 3;
//            Rect zoom = new Rect(cropW, cropH, width - cropW, height - cropH);
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
//            captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION,zoom );
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}