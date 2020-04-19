package com.example.chamikanandasiri.interactivebookreader;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
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
import java.util.ArrayList;
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

    ImageButton main_captureButton, main_arButton, main_storageButton, cap_dictionaryButton, cap_speechButton, cap_copyButton, cap_commentButton,
            cap_closeButton, spk_speakButton, spk_stopButton, spk_pauseButton, spk_closeButton, spk_backButton, dict_closeButton, dict_backButton,
            dict_saveButton, cmt_backButton, cmt_closeButton, cmt_saveButton;
    SurfaceView cameraView;
    CameraSource cameraSource;
    TextRecognizer textRecognizer;
    TextView main_detectedView, cap_displayView, spk_displayView, dict_displayView, dict_wordView, cmt_displayView;
    SeekBar speedBar, pitchBar;
    EditText cmt_editText, cmt_titleText;
    TextToSpeech speech;
    String detectedString, capturedString, selectedString, searchedWord;
    JSONObject dictionaryResponse;
    JSONArray searchResultDefinitions;
    Dialog capturePopup, speechPopup, dictionaryPopup, commentPopup;

    DataBaseHelper dbHelper;
    CommentHandler commentHandler;
    WordHandler wordHandler;

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

        dbHelper = new DataBaseHelper(this);
        commentHandler = new CommentHandler(dbHelper, this);
        wordHandler = new WordHandler(dbHelper, this);

        capturePopup = new Dialog(this);
        capturePopup.setContentView(R.layout.capture_popup);
        speechPopup = new Dialog(this);
        speechPopup.setContentView(R.layout.speech_popup);
        dictionaryPopup = new Dialog(this);
        dictionaryPopup.setContentView(R.layout.dictionary_popup);
        commentPopup = new Dialog(this);
        commentPopup.setContentView(R.layout.comment_popup);

        cameraView = findViewById(R.id.Surface);
        main_captureButton = findViewById(R.id.CaptureButton);
        main_arButton = findViewById(R.id.ARButton);
        main_storageButton = findViewById(R.id.StorageButton);
        main_detectedView = findViewById(R.id.DetectedTextView);

        setupCapturePopup();
        setupSpeechPopup();
        setupDictionaryPopup();
        setupCommentPopup();

        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        showTesting();

        detectText();
        constructText();

        speechInitialization();

        main_captureButton.setOnClickListener(v1 -> {
            capturedString = detectedString;
            showCapturePopup(v1);
        });

        main_storageButton.setOnClickListener(v1 -> loadStorageActivity());
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

    private void setupCommentPopup() {
        cmt_backButton = commentPopup.findViewById(R.id.CommentBackButton);
        cmt_closeButton = commentPopup.findViewById(R.id.CommentCloseButton);
        cmt_saveButton = commentPopup.findViewById(R.id.CommentOKButton);
        cmt_displayView = commentPopup.findViewById(R.id.CommentDisplayTextView);
        cmt_titleText = commentPopup.findViewById(R.id.CommentTitleEditText);
        cmt_editText = commentPopup.findViewById(R.id.CommentEditText);
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
        cap_commentButton.setOnClickListener(v2 -> {
            selectedString = getSelectedString(cap_displayView);
            capturePopup.dismiss();
            showCommentPopup(v2);
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
            searchedWord = "";
            searchResultDefinitions = null;
            dictionaryPopup.dismiss();
            showCapturePopup(v3);
        });
        dict_closeButton.setOnClickListener(v1 ->
        {
            searchedWord = "";
            dictionaryPopup.dismiss();
        });
        dict_saveButton.setOnClickListener(v1 -> {
            try {
                saveDictionaryDefinitions(searchResultDefinitions);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
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
        spk_pauseButton.setOnClickListener(v2 -> speechPause());
        spk_displayView.setText(selectedString);
        speechPopup.show();
    }

    public void showCommentPopup(View v) {
        cmt_backButton.setOnClickListener(v1 ->
        {
            commentPopup.dismiss();
            showCapturePopup(v1);
        });
        cmt_closeButton.setOnClickListener(v1 -> commentPopup.dismiss());
        cmt_saveButton.setOnClickListener(v1 -> saveComment());
        cmt_displayView.setText(selectedString);
        commentPopup.show();
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

    private void speechPause() {
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
            searchedWord = dictionaryResponse.getString("word");
            StringBuilder shower = new StringBuilder();
            searchResultDefinitions = dictionaryResponse.getJSONArray("definitions");
            Log.d("Test", searchResultDefinitions.toString());
            for (int i = 0; i < 5; i++) {
                shower.append(searchResultDefinitions.getJSONObject(i).getString("partOfSpeech")).append(" : ").append(searchResultDefinitions.getJSONObject(i).getString("definition")).append("\n");
            }
            dict_wordView.setText(searchedWord);
            dict_displayView.setText(shower.toString());
        } else {
            dict_displayView.setText("No Definitions found");
        }
    }

    private void saveComment() {
        String phrase = selectedString;
        String title = cmt_titleText.getText().toString();
        String comment = cmt_editText.getText().toString();
        CommentObject commentObject = new CommentObject(title, phrase, comment);
        commentHandler.addComment(commentObject);
    }

    private void saveDictionaryDefinitions(JSONArray array) throws JSONException {
        String word = selectedString;
        for(int i =0 ; i < array.length(); i++){
            JSONObject jo = array.getJSONObject(i);
            String def = jo.getString("definition");
            String pos = jo.getString("partOfSpeech");
            saveWord(word, def, pos);
        }
    }

    private void saveWord (String word, String definition, String pos) {
        WordObject wordObject = new WordObject(word, definition, pos);
        wordHandler.addWord(wordObject);
    }

    private void showTesting() {
        Log.d("Test", commentHandler.getComments().toString());
        Log.d("Test", wordHandler.getWords().toString());
        ArrayList<String[]> q = wordHandler.getDefinitionPosByWord("support");
        for (String[] w : q){
            Log.d("Test", w[0] + " : " + w[1] + "\n");
        }
    }

    private void loadStorageActivity() {
        Intent intent = new Intent(this,StorageActivity.class);
        startActivity(intent);
    }
}
