# Vulcan_GIX_Empathics_App

## Smart glasses Android App Environment Setting

- Android Studio Install:  `https://developer.android.com/studio/install`
- Create First Android App: `https://developer.android.com/training/basics/firstapp/creating-project`
  - activity_main.xml for app layout
  - AndroidManifest.xml for app permission
  - MainActivity.java for app entry
- Import Java library into App: 
  - Microsoft Speech-to-text Java SDK: 
    - View Sample code (MainActivity.java and Microphone.java) at
    `https://github.com/Azure-Samples/cognitive-services-speech-sdk/tree/master/samples/java/android/sdkdemo/app/src/main/java/com/microsoft/cognitiveservices/speech/samples/sdkdemo`
    - Use Recognize continuously code block starts at line 182 in MainActivity.java
    - Add `com.microsoft.cognitiveservices.speech:client-sdk:1.7.0` into build.gradle (Module:app)
    - Setup Microphone settings into: 16 kHz sample rate, 16 bit samples, mono (single-channel) only in Microphone.java
  - Microsoft Text Sentiment Analytics SDK: 
    - Download TextAnalyticsSDK library at `https://github.com/microsoft/Cognitive-TextAnalytics-Android`
    - Import TextAnalyticsSDK library into App folder: `https://developer.android.com/studio/projects/android-library#AddDependency` and import the library module to your project

- Vuzix Develop Centor: 
  - Create a Vuzix developer account at `https://www.vuzix.com/Developer`
  - Import Vuzix HUD Resources and HUD ActionMenu from `https://www.vuzix.com/Developer/Download/Blade` to Android App


## Smart glasses Features
### Camera
- Use Android Camera2 API at `https://developer.android.com/reference/android/hardware/camera2/package-summary`
- Several useful blogs: 
  - `https://pierrchen.blogspot.com/2015/01/android-camera2-api-explained.html`
  - `https://inducesmile.com/android/android-camera2-api-example-tutorial/`
  - `https://medium.com/androiddevelopers/understanding-android-camera-capture-sessions-and-requests-4e54d9150295`
### Speech-To-Text + Text Sentiment Analytics
- Create new SpeechRecognizer and start `startContinuousRecognitionAsync` during listening
- Text Sentiment feature in Sentiment.java class
- Add Listener in getSentimentScore function to update variable value in MainActivity.java
  - Implement Listener: `https://stackoverflow.com/questions/7157123/in-android-how-do-i-take-an-action-whenever-a-variable-changes/7157281#7157281`

### Intonation:
  - Overrite read function in Microphone.java
  - Copy byte buffer and write into DataOutputStream variable
  - Convert audio raw data to wav file: `https://stackoverflow.com/questions/30319193/android-how-to-convert-raw-data-of-audio-to-wav`
## Connect with App Service
### Retrofit2 + OKHttp3
  - Http Request in Class NetworkClient.java
    - Multipart to send Image
    - multipart/form-data to wrap image and other parameters into json for a http POST request
    - application/json to wrap Sentiment Score and Sequence_ID into json for a http POST request
## Async tasks and Periodical tasks keywords
  - ExecutorService
  - Handler
  - Thread
## Data transfer between Smart glasses and App Service
0. Get Session_Id from App Service, set Sequence_Id = 0
1. Upload Sentiment Score or Image or wav file to App Service and set sentiment flag or image flag or audio flag into True separately
2. Wait until ALL three results back from App Service
3. Upload Machine Learning Compute result, set all sentiment flag, image flag and audio flag to false for next Sequence_Id
4. Wait emotion result from App Service

## Output Feedback
- Implemented in MainActivity.java `setResultOnGlasses` function
### Visual
- Set two icons for positive and negative emotions
- Set imageView Resource into responding icon
- Set imageView into Visible

### Haptic 
- Add `implementation 'com.vuzix:haptics:1.0'` in build.gradle(Module:app) file
- Add `<uses-permission android:name="android.permission.VIBRATE" />` in AndroidManifest.xml file
- Create hapticsManager and assign Android Vibrator class with hapticsManager, ex: `private Vibrator mLeftVibe`, `mLeftVibe = hapticsManager.getLeftVibrator()`
- Implement `vibrate` function with responsing emotion result



    

  
