package com.example.myapplication;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.cognitive.textanalytics.model.request.RequestDocIncludeLanguage;
import com.microsoft.cognitive.textanalytics.model.request.keyphrases_sentiment.TextRequest;
import com.microsoft.cognitive.textanalytics.model.response.sentiment.SentimentResponse;
import com.microsoft.cognitive.textanalytics.retrofit.ServiceCall;
import com.microsoft.cognitive.textanalytics.retrofit.ServiceCallback;
import com.microsoft.cognitive.textanalytics.retrofit.ServiceRequestClient;
import com.microsoft.cognitiveservices.speech.SpeechConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import retrofit2.Call;
import retrofit2.Response;

public class Sentiment {
    private static final String TAG = "Empethics/Sentiment";
    public ServiceRequestClient mRequest;
    private String SentimentSubscriptionKey = "bd009590e8754a29b599a89eb0102f55";
    private static final String SpeechSubscriptionKey = "d122e91d2df24ce889a13695542564c2";
    private static final String SpeechRegion = "eastus";
    private ServiceCall mSentimentCall;
    private ServiceCallback mSentimentCallback;
    private SpeechConfig speechConfig;
    private RequestDocIncludeLanguage mDocIncludeLanguage;
    private TextRequest mTextIncludeLanguageRequest;               // request for key phrases and sentiment analysis
    private MicrophoneStream microphoneStream;
    public String sentimentResult = "";

    public void afterTextchange(String textInputString) {
        mDocIncludeLanguage = new RequestDocIncludeLanguage();
        mDocIncludeLanguage.setId("1");
        mDocIncludeLanguage.setLanguage("en");
        mDocIncludeLanguage.setText(textInputString);
        List<RequestDocIncludeLanguage> textDocs = new ArrayList<>();
        textDocs.add(mDocIncludeLanguage);
        mTextIncludeLanguageRequest = new TextRequest(textDocs);
    }

    //java native interface

    public String getSentimentScore() {

            mRequest = new ServiceRequestClient(SentimentSubscriptionKey);
            ServiceCallback mSentimentCallback = new ServiceCallback(mRequest.getRetrofit()) {
                @Override
                public void onResponse(Call call, Response response) {
                    super.onResponse(call, response);
                    SentimentResponse sentimentResponse = (SentimentResponse) response.body();
                    Log.d(TAG, String.valueOf(sentimentResponse.getDocuments()));

                    if (response != null && response.isSuccessful()) {
                        Log.d(TAG, String.valueOf(sentimentResponse.getDocuments().get(0).getScore()));
                        sentimentResult = sentimentResponse.getDocuments().get(0).getScore().toString();
                        if(MainActivity.scorelistener!=null){
                            MainActivity.scorelistener.onChanged(sentimentResult);
                        }
                    }
                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    super.onFailure(call, t);
                    Log.d(TAG, "Fail");
                }
            };

            try {
                mSentimentCall = mRequest.getSentimentAsync(mTextIncludeLanguageRequest, mSentimentCallback);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Fail in catch");
                System.out.println(e);
            }
            return sentimentResult;
    };

}
