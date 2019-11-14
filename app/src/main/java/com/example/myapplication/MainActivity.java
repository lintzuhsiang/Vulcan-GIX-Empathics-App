package com.example.myapplication;

import androidx.annotation.NonNull;
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
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.Handler;
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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionEventArgs;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.util.EventHandler;
import com.vuzix.hud.actionmenu.ActionMenuActivity;

import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;
import com.microsoft.cognitive.textanalytics.retrofit.ServiceCall;
import com.microsoft.cognitive.textanalytics.retrofit.ServiceCallback;
import com.microsoft.cognitive.textanalytics.retrofit.ServiceRequestClient;

//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.FileBody;
//import org.apache.http.entity.mime.content.StringBody;


public class MainActivity extends ActionMenuActivity {

    private static final String TAG = "MainActivity";

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

    //timestamp
    protected long cameraCaptureStartTime = 0;
    protected long faceAPIStartTime = 0;

    //face API
    String FACE_SUBSCRIPTION_KEY = "bc027dc227484433a77d7b613807d230";
    String FACE_ENDPOINT = "https://empthetic.cognitiveservices.azure.com/face/v1.0";
    private final String apiEndpoint = FACE_ENDPOINT;
    private final String subscriptionKey = FACE_SUBSCRIPTION_KEY;
    private final FaceServiceClient faceServiceClient = new FaceServiceRestClient(apiEndpoint, subscriptionKey);
    public String fileDir;
    public SimpleDateFormat DateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    private MicrophoneStream microphoneStream;


    private MicrophoneStream createMicrophoneStream() {
        if (microphoneStream != null) {
            microphoneStream.close();
            microphoneStream = null;
        }

        microphoneStream = new MicrophoneStream();
        return microphoneStream;
    }

    //Speech-to-Text and Text Sentiment API
    private static final String SpeechSubscriptionKey = "d122e91d2df24ce889a13695542564c2";
    private static final String SpeechRegion = "eastus";
    private ServiceCall mSentimentCall;
    private String preSpeechResult;

    private SpeechConfig speechConfig;
    private String sentimentResult = "";


    NetworkClient client = new NetworkClient();
    Sentiment sentiment = new Sentiment();
    boolean continuousListeningStarted = false;
    SpeechRecognizer reco = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureView = findViewById(R.id.textureView);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        takePictureButton = findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;
        fileDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        imageView = findViewById(R.id.imageView);
        imageViewRedDot = findViewById(R.id.imageView2);


        Toast.makeText(MainActivity.this, "Tap to startm emotion detection.", Toast.LENGTH_LONG).show();

//        mRequest = new ServiceRequestClient(SentimentSubscriptionKey);


        try {
            speechConfig = SpeechConfig.fromSubscription(SpeechSubscriptionKey, SpeechRegion);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return;
        }


        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (continuousListeningStarted) {
                    if (reco != null) {
                        final Future<Void> task = reco.stopContinuousRecognitionAsync();
                        microphoneStream.stopRecording();
                        micHandler.removeCallbacks(micRunnable);

                        setSSTCompletedListener(task, new OnTaskCompletedListener<Void>() {
                            @Override
                            public void onCompleted(Void result) {
                                Log.i("mic", "Continuous recognition stopped.");
                            }
                        });
                        continuousListeningStarted = false;
                    } else {
                        continuousListeningStarted = false;
                    }


                    Toast.makeText(MainActivity.this, "Emotion detection: Off", Toast.LENGTH_SHORT).show();
                    timerHandler.removeCallbacks(runnableCode);
//                        ss_executorService.shutdown();
//                        s_executorService.shutdown();
                    int imageResource = getResources().getIdentifier("@drawable/pause", "drawable", getPackageName());
                    imageViewRedDot.setImageResource(imageResource);
                    imageView.setVisibility(View.INVISIBLE);

                    imageViewRedDot.setVisibility(View.INVISIBLE);
                    Log.d("Handlers", "Stop runnable on main thread");

                } else {

                    int imageResource = getResources().getIdentifier("@drawable/reddot", "drawable", getPackageName());
                    imageViewRedDot.setImageResource(imageResource);

                    Toast.makeText(MainActivity.this, "Emotion detection: On", Toast.LENGTH_SHORT).show();
                    imageViewRedDot.setVisibility(View.VISIBLE);
                    //post_picture();

                    //////microphone
                    post_mic();

                }
            }
        });
    }


    private void post_picture() {
        runnableCode = new Runnable() {
            @Override
            public void run() {
                Log.d("Handlers", "Called on main thread");
                cameraCaptureStartTime = System.currentTimeMillis();
                takePicture();
//
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(runnableCode);


//        RepeatTakePhoto(new OnTaskCompletedListener() {
//            @Override
//            public void onCompleted(Object result) {
//            }
//        });
//
    }

    private void post_mic() {
        speechSentiment();
        uploadScore();
    }

    private String speechSentiment() {

        AudioConfig audioInput = null;
        try {
            Log.i("mic", "Continuous recognition started.");
            audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
            reco = new SpeechRecognizer(speechConfig, audioInput);

            final String currTime = fileDir + "/" + DateFormat.format(new Date());
            final boolean isRecording = true;
//            microphoneStream.startRecording(isRecording, currTime);

            micRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        microphoneStream.startRecording(currTime);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    micHandler.postDelayed(micRunnable, 2000);

                }
            };
            micHandler.post(micRunnable);
//            Thread audio = new Thread(micRunnable);
//            audio.start();
            Log.d("mic", String.valueOf(microphoneStream));


            reco.recognized.addEventListener(new EventHandler<SpeechRecognitionEventArgs>() {
                @Override
                public void onEvent(Object o, SpeechRecognitionEventArgs speechRecognitionResultEventArgs) {
                    final String s = speechRecognitionResultEventArgs.getResult().getText();
                    Log.i("text", "Final result received: " + s);
                    if (s.length() > 10) {
                        sentiment.afterTextchange(s);
                        sentimentResult = sentiment.getSentimentScore();
                        if (preSpeechResult != s && mlistener != null) {
                            mlistener.onChange(s);
                        }

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
        return sentimentResult;
    }


    private <T> void setSSTCompletedListener(final Future<T> task, final OnTaskCompletedListener<T> listener) {
        ms_executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
//                T result = task.get();
//                listener.onCompleted(result);
                return null;
            }
        });
    }

    private interface OnTaskCompletedListener<T> {
        void onCompleted(T taskResult);
    }

    private static ExecutorService ms_executorService;

    static {
        ms_executorService = Executors.newCachedThreadPool();
    }


    private TextChangeListener mlistener;

    private void uploadScore() {
        s_executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                client.uploadScore("0.5");
                setTextListener(new TextChangeListener<String>() {
                    @Override
                    public void onChange(String result) {
                        sentiment.afterTextchange(result);
                        sentimentResult =  sentiment.getSentimentScore();
                        Log.d("sentiment", result);
                        client.uploadScore(result);
                    }
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void setTextListener(TextChangeListener<String> listener) {
        mlistener = listener;
    }

    private interface TextChangeListener<String> {
        void onChange(String result);
    }

    private static ScheduledExecutorService s_executorService;

    static {
        s_executorService = Executors.newScheduledThreadPool(1);
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
            //This is called when the camera is open
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


    private File takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return null;
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

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
            Date now = new Date();

            file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + formatter.format(now) + ".jpg");


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
                            Log.d("emotion", "image close");
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
                            Log.d("emotion", file.getAbsolutePath());
//                            client.uploadImage(file);
                            //detectAndFrame(storedBitmap);
                            output.flush();
                            output.close();
                            Log.d("emotion", "saved image");
                        }
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.d("emotion", String.format("takePicture end %s", cameraCaptureStartTime - System.currentTimeMillis()));
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
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return file;
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
    protected void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
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

    // detect capture frame and call Face API
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


}