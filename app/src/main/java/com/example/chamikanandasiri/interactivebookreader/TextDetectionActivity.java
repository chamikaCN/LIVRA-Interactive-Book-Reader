package com.example.chamikanandasiri.interactivebookreader;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TextDetectionActivity extends AppCompatActivity {

    ImageButton main_captureButton, main_backButton, cap_dictionaryButton, cap_speechButton, cap_commentButton,
            cap_closeButton, spk_speakButton, spk_stopButton, spk_pauseButton, spk_closeButton, spk_backButton,
            dict_closeButton, dict_backButton, dict_saveButton, cmt_backButton, cmt_closeButton, cmt_saveButton;

    SurfaceView cameraView;
    CameraSource cameraSource;

    TextRecognizer textRecognizer;
    ToneGenerator toneGenerator;

    TextView cap_displayView, spk_displayView, dict_displayView, dict_wordView, cmt_displayView;
    EditText cmt_editText, cmt_titleText;
    String detectedString, capturedString, selectedString, searchedWord;
    JSONObject dictionaryResponse;
    JSONArray searchResultDefinitions;
    Dialog capturePopup, speechPopup, dictionaryPopup, commentPopup;

    DataBaseHelper dbHelper;
    CommentHandler commentHandler;
    WordHandler wordHandler;
    BookHandler bookHandler;
    ContentHandler contentHandler;
    Speaker speaker;
    SharedPreferences sharedPreferences;

    String currentTheme;

    int timeStampUniqueCount = 0;
    float speechSpeedValue, speechPitchValue;

    private String TAG = "Test";

    final int RequestCameraPermissionID = 1001;
    final int RequestWriteStoragePermissionID = 1002;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
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
            case RequestWriteStoragePermissionID: {

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = this.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        currentTheme = sharedPreferences.getString("Theme", "Light");
        speechPitchValue = sharedPreferences.getFloat("Pitch", 0.5f);
        speechSpeedValue = sharedPreferences.getFloat("Speed", 0.5f);
        if (currentTheme.equals("Light")) {
            setTheme(R.style.LightTheme);
        } else if (currentTheme.equals("Dark")) {
            setTheme(R.style.DarkTheme);
        }

        setContentView(R.layout.activity_text_detection);

        dbHelper = new DataBaseHelper(this);
        commentHandler = new CommentHandler(dbHelper, this);
        wordHandler = new WordHandler(dbHelper, this);
        bookHandler = new BookHandler(dbHelper, this);
        contentHandler = new ContentHandler(dbHelper, this);
        speaker = new Speaker(this);

        setupMainLayout();
        setupAllPopups();

        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        //multiDetector = new MultiDetector.Builder().add(textRecognizer).add(barcodeDetector).build();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        detectText();
        constructText();

    }

    //UI components Initialization

    private void setupMainLayout() {

        cameraView = findViewById(R.id.Surface);
        main_captureButton = findViewById(R.id.CaptureButton);
        main_backButton = findViewById(R.id.TextDetectionBackButton);

        main_captureButton.setOnClickListener(v1 -> {
            capturedString = detectedString;
            showCapturePopup(v1);
        });
        main_backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        });
    }

    private void setupAllPopups() {
        capturePopup = new Dialog(this);
        capturePopup.setContentView(R.layout.popup_capture);
        speechPopup = new Dialog(this);
        speechPopup.setContentView(R.layout.popup_speech);
        dictionaryPopup = new Dialog(this);
        dictionaryPopup.setContentView(R.layout.popup_dictionary);
        commentPopup = new Dialog(this);
        commentPopup.setContentView(R.layout.popup_comment);

        setupCapturePopup();
        setupSpeechPopup();
        setupDictionaryPopup();
        setupCommentPopup();
    }

    private void setupCapturePopup() {
        cap_dictionaryButton = capturePopup.findViewById(R.id.DictionaryButton);
        cap_speechButton = capturePopup.findViewById(R.id.SpeechButton);
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
                buttonAnimation1(dict_saveButton);
                saveDictionaryDefinitions(searchResultDefinitions);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        dictionaryPopup.show();
    }

    public void showSpeechPopup(View v) {

        spk_backButton.setOnClickListener(v1 -> {
            speechStop();
            speechPopup.dismiss();
            showCapturePopup(v1);
        });
        spk_closeButton.setOnClickListener(v1 ->
        {
            speechStop();
            speechPopup.dismiss();
        });
        spk_speakButton.setOnClickListener(v2 -> {
            buttonAnimation1(spk_speakButton);
            speak(selectedString);
        });
        spk_stopButton.setOnClickListener(v2 -> {
            buttonAnimation1(spk_stopButton);
            speechStop();
        });
        spk_pauseButton.setOnClickListener(v2 -> speechPause());
        spk_displayView.setText(selectedString);
        speechPopup.show();
    }

    public void showCommentPopup(View v) {
        cmt_backButton.setOnClickListener(v1 ->
        {
            cmt_displayView.setText("");
            cmt_titleText.setText("");
            cmt_editText.setText("");
            commentPopup.dismiss();
            showCapturePopup(v1);
        });
        cmt_closeButton.setOnClickListener(v1 -> commentPopup.dismiss());
        cmt_saveButton.setOnClickListener(v1 -> {
            buttonAnimation1(cmt_saveButton);
            saveComment();
        });
        cmt_displayView.setText(selectedString);
        commentPopup.show();
    }

    // Main Functions

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
                            ActivityCompat.requestPermissions(TextDetectionActivity.this,
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
                    main_captureButton.post(() -> {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < items.size(); i++) {
                            TextBlock item = items.valueAt(i);
                            stringBuilder.append(item.getValue());
                            stringBuilder.append("\n");
                        }
                        detectedString = stringBuilder.toString();
                    });
                } else {
                    detectedString = "";
                }
            }
        });
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
                try {
                    dictionaryResponse = new JSONObject(message);
                    formatDictionaryResponse();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void formatDictionaryResponse() throws JSONException {
        if (dictionaryResponse == null) {
            String s = "cannot connect with server";
            dict_displayView.setText(s);
        } else if (dictionaryResponse.has("word")) {
            searchedWord = dictionaryResponse.getString("word");
            StringBuilder shower = new StringBuilder();
            searchResultDefinitions = dictionaryResponse.getJSONArray("definitions");
            int limit = Integer.min(5, searchResultDefinitions.length());
            for (int i = 0; i < limit; i++) {
                shower.append(searchResultDefinitions.getJSONObject(i).getString("partOfSpeech")).append(" : ").append(searchResultDefinitions.getJSONObject(i).getString("definition")).append("\n");
            }
            dict_wordView.setText(searchedWord);
            dict_displayView.setText(shower.toString());
        } else {
            String s = "No Definitions found";
            dict_displayView.setText(s);
        }
    }

    //Supporting Functions

    private void speak(String s) {
        speaker.speak(s, speechSpeedValue, speechPitchValue);
    }

    private void speechPause() {
    }

    private void speechStop() {
        speaker.stop();
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
        int limit = Integer.min(5, array.length());
        for (int i = 0; i < limit; i++) {
            JSONObject jo = array.getJSONObject(i);
            String def = jo.getString("definition");
            String pos = jo.getString("partOfSpeech");
            saveWord(word, def, pos);
        }
    }

    private void saveWord(String word, String definition, String pos) {
        WordObject wordObject = new WordObject(word, definition, pos, timeStampUniqueCount);
        Log.d(TAG, "saveWord: " + word + definition + pos + timeStampUniqueCount);
        if (timeStampUniqueCount == 9) {
            timeStampUniqueCount = 0;
        } else {
            timeStampUniqueCount += 1;
        }
        wordHandler.addWord(wordObject);
    }

    //Reusable Functions

    private void loadImage(String Url, ImageView im) {
        Picasso.with(this).load(Url)
                .placeholder(R.drawable.bookcover_loading_anim)
                .into(im, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                    }
                });
    }

    public String[] JSONArrayToStringArray(JSONArray array) {
        int len = array.length();
        String[] newArray = new String[len];
        for (int j = 0; j < len; j++) {
            String s;
            try {
                s = array.getString(j);
                newArray[j] = s;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return newArray;
    }

    private String getSelectedString(TextView view) {
        String s = view.getText().subSequence(view.getSelectionStart(), view.getSelectionEnd()).toString();
        if (s.equals("")) {
            return view.getText().toString();
        }
        return s;
    }

    private void copyToClipboard() {

        selectedString = getSelectedString(cap_displayView);
        ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData myClip = ClipData.newPlainText("text", selectedString);
        myClipboard.setPrimaryClip(myClip);

        Toast.makeText(getApplicationContext(), "Text Copied", Toast.LENGTH_SHORT).show();

    }

    private void buttonAnimation1(ImageButton button) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        BounceInterpolator bi = new BounceInterpolator(0.2, 20);
        animation.setInterpolator(bi);
        button.startAnimation(animation);
    }

    private void buttonAnimation2(ImageButton button) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.constant_bounce);
        button.startAnimation(animation);
    }

    private void buttonAnimation3(ImageButton button) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.idle);
        button.startAnimation(animation);
    }

    private void buttonAnimation4(Button button) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        BounceInterpolator bi = new BounceInterpolator(0.2, 20);
        animation.setInterpolator(bi);
        button.startAnimation(animation);
    }
}
