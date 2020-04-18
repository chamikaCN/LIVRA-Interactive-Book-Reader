package com.example.chamikanandasiri.interactivebookreader;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ImageButton main_captureButton, main_arButton, cap_dictionaryButton, cap_speechButton, cap_copyButton, cap_commentButton,
            cap_closeButton, spk_speakButton, spk_stopButton, spk_pauseButton, spk_closeButton, spk_backButton, dict_closeButton, dict_backButton,
            dict_saveButton;
    SurfaceView cameraView;
    CameraSource cameraSource;
    TextRecognizer textRecognizer;
    TextView main_detectedView, cap_displayView, spk_displayView, dict_displayView, dict_wordView;
    SeekBar speedBar, pitchBar;
    TextToSpeech speech;
    String detectedString, capturedString, selectedString;
    JSONObject dictionaryResponse;
    Dialog capturePopup, speechPopup, dictionaryPopup;

    final int RequestCameraPermissionID = 1001;

    //check whether camera access permission is given
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCameraPermissionID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        capturePopup = new Dialog(this);
        capturePopup.setContentView(R.layout.capture_popup);
        speechPopup = new Dialog(this);
        speechPopup.setContentView(R.layout.speech_popup);
        dictionaryPopup = new Dialog(this);
        dictionaryPopup.setContentView(R.layout.dictionary_popup);

        cameraView = findViewById(R.id.Surface);
        main_captureButton = findViewById(R.id.CaptureButton);
        main_arButton = findViewById(R.id.ARButton);
        main_detectedView = findViewById(R.id.DetectedTextView);

        setupCapturePopup();
        setupSpeechPopup();
        setupDictionaryPopup();

        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        detectText();
        constructText();

        speechInitialization();

        main_captureButton.setOnClickListener(v1 -> {
            capturedString = detectedString;
            showCapturePopup(v1);
        });

    }

    private void setupCapturePopup() {
        cap_dictionaryButton = capturePopup.findViewById(R.id.DictionaryButton);
        cap_speechButton = capturePopup.findViewById(R.id.SpeechButton);
        cap_copyButton = capturePopup.findViewById(R.id.CopyButton);
        cap_commentButton = capturePopup.findViewById(R.id.CommentButton);
        cap_closeButton = capturePopup.findViewById(R.id.CloseButton);
        cap_displayView = capturePopup.findViewById(R.id.DisplayTextView);
    }

    private void setupSpeechPopup() {
        spk_speakButton = speechPopup.findViewById(R.id.SpeakButton);
        spk_stopButton = speechPopup.findViewById(R.id.StopButton);
        spk_pauseButton = speechPopup.findViewById(R.id.PauseButton);
        spk_closeButton = speechPopup.findViewById(R.id.SpeakCloseButton);
        spk_backButton = speechPopup.findViewById(R.id.SpeakBackButton);
        spk_displayView = speechPopup.findViewById(R.id.SpeakDisplayTextView);
        speedBar = speechPopup.findViewById(R.id.SpeedSeekBar);
        pitchBar = speechPopup.findViewById(R.id.PitchSeekBar);
    }

    private void setupDictionaryPopup() {
        dict_closeButton = dictionaryPopup.findViewById(R.id.DictionaryCloseButton);
        dict_backButton = dictionaryPopup.findViewById(R.id.DictionaryBackButton);
        dict_saveButton = dictionaryPopup.findViewById(R.id.SaveButton);
        dict_displayView = dictionaryPopup.findViewById(R.id.DictionaryDisplayTextView);
        dict_wordView = dictionaryPopup.findViewById(R.id.DictionaryWordTextView);
    }

    public void showCapturePopup(View v) {

        cap_closeButton.setOnClickListener(v1 -> capturePopup.dismiss());
        cap_speechButton.setOnClickListener(v2 -> {
            selectedString = getSelectedString(cap_displayView);
            capturePopup.dismiss();
            showSpeechPopup(v2);
        });
        cap_dictionaryButton.setOnClickListener(v2 -> {
            selectedString = getSelectedString(cap_displayView);
            capturePopup.dismiss();
            showDictionaryPopup(v2);
        });
        cap_copyButton.setOnClickListener(v1 -> copyToClipboard());
        cap_displayView.setText(capturedString);

        capturePopup.show();

    }

    public void showDictionaryPopup(View v) {

        getDictionaryResponse(selectedString);
        dict_backButton.setOnClickListener(v3 -> {
            dict_displayView.setText("");
            dict_wordView.setText("");
            dictionaryPopup.dismiss();
            showCapturePopup(v3);
        });
        dict_closeButton.setOnClickListener(v1 -> dictionaryPopup.dismiss());
        dictionaryPopup.show();

    }

    public void showSpeechPopup(View v) {

        spk_backButton.setOnClickListener(v1 -> {
            speech.stop();
            speechPopup.dismiss();
            showCapturePopup(v1);
        });
        spk_closeButton.setOnClickListener(v1 ->
        {
            speech.stop();
            speechPopup.dismiss();
        });
        spk_speakButton.setOnClickListener(v2 -> speak(selectedString));
        spk_stopButton.setOnClickListener(v2 -> speech.stop());
        spk_pauseButton.setOnClickListener(v2 -> {});
        spk_displayView.setText(selectedString);
        speechPopup.show();

    }

    private void detectText() {

        if (!textRecognizer.isOperational()) {
            Log.w("TextDetectionActivity", "Detector dependencies are not yet available");
        } else {
            //creating a video stream through camera
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(350, 350)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });
        }
    }

    private void constructText() {

        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {

            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                final SparseArray<TextBlock> items = detections.getDetectedItems();
                if (items.size() != 0) {
                    main_detectedView.post(() -> {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < items.size(); i++) {
                            TextBlock item = items.valueAt(i);
                            stringBuilder.append(item.getValue());
                            stringBuilder.append("\n");
                        }
                        detectedString = stringBuilder.toString();
                    });
                }
            }
        });
    }

    private String getSelectedString(TextView view) {
        String s = view.getText().subSequence(view.getSelectionStart(), view.getSelectionEnd()).toString();
        if (s.equals("")) {
            return view.getText().toString();
        }
        return s;
    }

    private void speechInitialization() {

        speech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = speech.setLanguage(Locale.UK);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "language not supported");
                } else {
                    spk_speakButton.setEnabled(true);
                }
            } else {
                Log.e("TTS", "initializing failed");
            }
        }
        );
    }

    private void speak(String s) {
        float speedValue = (float) speedBar.getProgress() / 50;
        if (speedValue < 0.1) speedValue = 0.1f;
        float pitchValue = (float) pitchBar.getProgress() / 50;
        if (pitchValue < 0.1) pitchValue = 0.1f;
        speech.setPitch(pitchValue);
        speech.setSpeechRate(speedValue);
        speech.speak(s, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void copyToClipboard() {

        selectedString = getSelectedString(cap_displayView);
        ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData myClip = ClipData.newPlainText("text", selectedString);
        myClipboard.setPrimaryClip(myClip);

        Toast.makeText(getApplicationContext(), "Text Copied", Toast.LENGTH_SHORT).show();

    }

    private void getDictionaryResponse(String word) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://wordsapiv1.p.rapidapi.com/words/" + word + "/definitions")
                .get()
                .addHeader("x-rapidapi-host", "wordsapiv1.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "767af11e35mshef5bdaa696c329fp1d388fjsnf7322ac9056b")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String message = e.getMessage();
                Log.w("failure Response", message);
                dictionaryResponse = null;
                try {
                    formatDictionaryResponse();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String message = response.body().string();
                Log.e("Test", message);
                try {
                    dictionaryResponse = new JSONObject(message);
                    Log.d("Test", "werty");
                    formatDictionaryResponse();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void formatDictionaryResponse() throws JSONException {
        if (dictionaryResponse == null) {
            dict_displayView.setText("cannot connect with server");
        } else if (dictionaryResponse.has("word")) {
            String word = dictionaryResponse.getString("word");
            String shower = "";
            JSONArray definitions = dictionaryResponse.getJSONArray("definitions");
            Log.d("Test", definitions.toString());
            for (int i = 0; i < 5; i++) {
                shower = shower + definitions.getJSONObject(i).getString("partOfSpeech") + " : " + definitions.getJSONObject(i).getString("definition") + "\n";
            }
            dict_wordView.setText(word);
            dict_displayView.setText(shower);
        } else {
            dict_displayView.setText("No Definitions found");
        }
    }
}
