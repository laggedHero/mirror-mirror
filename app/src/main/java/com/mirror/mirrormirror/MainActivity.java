package com.mirror.mirrormirror;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.cameraview.CameraView;
import com.mirror.mirrormirror.services.MirrorMirrorService;
import com.mirror.mirrormirror.services.MirrorService;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_PERMISSIONS = 1;

    private static final String KEYWORD = "mirror mirror";

    private CameraView cameraView;
    private TextView listeningWarning;
    private TextView feedbackMessage;
    private ProgressBar progressBar;

    private TextView debugMessage;
    private TextView debugWarning;
    private ImageView debugImage;

    private StringBuilder stringBuilder = new StringBuilder();

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;

    private boolean isAboutToShow = false;

    private RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            listeningWarning.setVisibility(View.VISIBLE);
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
            String message;
            boolean restart = true;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "Audio recording error";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "Client side error";
                    restart = false;
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "Insufficient permissions";
                    restart = false;
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "Network error";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "Network timeout";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "No match";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RecognitionService busy";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "error from server";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "No speech input";
                    break;
                default:
                    message = "Not recognised";
                    break;
            }

            debugWarning.setText(message);

            if (restart) {
                stopSpeechRecognition();
                startSpeechRecognition();
            }
        }

        @Override
        public void onResults(Bundle results) {
            // Restart new dictation cycle
            startSpeechRecognition();

            // Return to the container activity dictation results
            final List<String> stringList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (stringList != null) {
                stringBuilder.setLength(0);
                for (int i = 0, size = stringList.size(); i < size; i++) {
                    stringBuilder.append(stringList.get(i));
                    stringBuilder.append(" : ");
                }

                debugMessage.setText(stringBuilder.toString());

                checkKeywordAndDispatchCall(stringList);
            }
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }
    };

    private CameraView.Callback cameraCallback = new CameraView.Callback() {
        @Override
        public void onPictureTaken(CameraView cameraView, byte[] data) {
            callService(data);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        cameraView = (CameraView) findViewById(R.id.camera);
        listeningWarning = (TextView) findViewById(R.id.listeningWarning);
        feedbackMessage = (TextView) findViewById(R.id.feedbackMessage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        debugMessage = (TextView) findViewById(R.id.debugMessage);
        debugWarning = (TextView) findViewById(R.id.debugWarning);
        debugImage = (ImageView) findViewById(R.id.debugImage);

        cameraView.addCallback(cameraCallback);

        prepFeedbackMessage();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            cameraView.start();
            startTextToSpeech();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSIONS);
        }
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        stopTextToSpeech();
        stopSpeechRecognition();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (permissions.length != 2 || grantResults.length != 2) {
                    throw new RuntimeException("Error on requesting permissions.");
                }
                // No need to start camera here; it is handled by onResume
                break;
        }
    }

    private SpeechRecognizer getSpeechRecognizer() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(recognitionListener);
        }

        return speechRecognizer;
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        getSpeechRecognizer().startListening(intent);
    }

    private void stopSpeechRecognition() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        listeningWarning.setVisibility(View.GONE);
    }

    private void startTextToSpeech() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.ERROR) {
                    // cry
                    return;
                }
                textToSpeech.setSpeechRate(0.75f);
                startSpeechRecognition();
            }
        });
    }

    private void stopTextToSpeech() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
    }

    private void checkKeywordAndDispatchCall(List<String> stringList) {
        if (isAboutToShow) {
            return;
        }

        for (int i = 0, size = stringList.size(); i < size; i++) {
            if (KEYWORD.equalsIgnoreCase(stringList.get(i))) {
                isAboutToShow = true;
                cameraView.takePicture();
                break;
            }
        }
    }

    private void callService(byte[] photoData) {
        Bitmap bMap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
        debugImage.setImageBitmap(bMap);
        isAboutToShow = false;

        textToSpeech.speak("You're looking great", TextToSpeech.QUEUE_FLUSH, null, "mirror-talk-back");

        feedbackMessage.setText("You're looking great You're looking great You're looking great You're looking great You're looking great You're looking great You're looking great You're looking great You're looking great");

        feedbackMessage.animate()
                .alpha(1f)
                .translationYBy(-750)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(300);

        feedbackMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                feedbackMessage.animate()
                        .alpha(0f)
                        .translationYBy(750)
                        .scaleX(0.5f)
                        .scaleY(0.5f)
                        .setDuration(300);
            }
        }, 3300);
    }

    private void prepFeedbackMessage() {
        feedbackMessage.animate()
                .alpha(0f)
                .translationYBy(750)
                .scaleX(0.5f)
                .scaleY(0.5f)
                .setDuration(300);
    }

    private void callItForReal(byte[] photoData) {
        progressBar.setVisibility(View.VISIBLE);

        MirrorService mirrorService = MirrorMirrorService.getService();

        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), photoData);

        MultipartBody.Part photo =
                MultipartBody.Part.createFormData("picture", " picture", requestFile);

        mirrorService.sendPhoto(photo).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
