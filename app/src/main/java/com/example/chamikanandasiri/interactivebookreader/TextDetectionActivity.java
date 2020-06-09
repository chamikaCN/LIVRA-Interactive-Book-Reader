package com.example.chamikanandasiri.interactivebookreader;


import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class TextDetectionActivity extends AppCompatActivity {

    ImageButton main_captureButton, main_backButton, cap_dictionaryButton, cap_speechButton, cap_commentButton,
            cap_closeButton, spk_speakButton, spk_stopButton, spk_closeButton, spk_backButton,
            dict_closeButton, dict_backButton, dict_saveButton, cmt_backButton, cmt_closeButton, cmt_saveButton;

    TextRecognizer textRecognizer;
    ToneGenerator toneGenerator;
    Camera mCamera;
    TextView cap_displayView, spk_displayView, dict_displayView, dict_wordView, cmt_displayView;
    EditText cmt_editText, cmt_titleText;
    String capturedString, currentTheme, searchedWord;
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

    private static String selectedString;

    int timeStampUniqueCount = 0;
    float speechSpeedValue, speechPitchValue;
    private HashMap<String, Integer> textSizeConfig;
    private String TAG = "Test";
    private String textSize;

    final int RequestCameraPermissionID = 1001;
    final int RequestWriteStoragePermissionID = 1002;
    final File pictureFile = new File("/data/data/com.example.chamikanandasiri.interactivebookreader/files", "croppedimage" + ".jpg");  //TODO :only for testing purpose. comment this out before final built


    private CameraPreview preview;
    private final Matrix mat = new Matrix();
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            try {
                Rect r = preview.getFocusArea();
                Log.d("rectangle", r.toString());

                Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
                Log.d("Bitmap-Height", String.valueOf(b.getHeight()));
                Log.d("Bitmap-Width", String.valueOf(b.getWidth()));
                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), mat, true);

                Log.d("Bitmap-Height", String.valueOf(b.getHeight()));
                Log.d("Bitmap-Width", String.valueOf(b.getWidth()));

                Log.d("rectangle-left", String.valueOf(r.left));
                Log.d("rectangle-top", String.valueOf(r.top));
                Log.d("rectangle-right", String.valueOf(r.right));
                Log.d("rectangle-bottom", String.valueOf(r.bottom));

                Bitmap croppedBitmap = Bitmap.createBitmap(b, r.left, r.top, r.right - r.left, r.bottom - r.top);
                constructText(croppedBitmap);

                //TODO :only for testing purpose. comment out next 3 lines before final built

                FileOutputStream fos = new FileOutputStream(pictureFile);
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            Log.v(TAG, "will now release camera");
//            mCamera.release();
            Log.v(TAG, "will now call finish()");
//            finish();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //seting view to be full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) this).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        sharedPreferences = this.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        currentTheme = sharedPreferences.getString("Theme", "Light");
        speechPitchValue = sharedPreferences.getFloat("Pitch", 0.5f);
        speechSpeedValue = sharedPreferences.getFloat("Speed", 0.5f);
        textSize = sharedPreferences.getString("TextSize", "Medium");
        textSizeConfig = new HashMap<>();
        textSizeConfig.put("Small", 12);
        textSizeConfig.put("Medium", 15);
        textSizeConfig.put("Large", 18);

        if (currentTheme.equals("Light")) {
            setTheme(R.style.LightTheme);
        } else if (currentTheme.equals("Dark")) {
            setTheme(R.style.DarkTheme);
        }

        setContentView(R.layout.activity_text_detection);
        //getting the camera instance
        mCamera = getCameraInstance();
        //creating a camera preview
        preview = new CameraPreview(this, mCamera);

//     setting to rotation value to 90 . thiss will later used to rotate the passed frame
        mat.postRotate(90);

        FrameLayout previewFrame = (FrameLayout) findViewById(R.id.camera_preview);
        previewFrame.addView(preview);

        dbHelper = new DataBaseHelper(this);
        commentHandler = new CommentHandler(dbHelper, this);
        wordHandler = new WordHandler(dbHelper, this);
        bookHandler = new BookHandler(dbHelper, this);
        contentHandler = new ContentHandler(dbHelper, this);
        speaker = new Speaker(this);

        setupMainLayout();
        setupAllPopups();

        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    }


    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    //UI components Initialization

    private void setupMainLayout() {

        main_captureButton = findViewById(R.id.CaptureButton);
        main_backButton = findViewById(R.id.TextDetectionBackButton);

        main_captureButton.setOnClickListener(v1 -> {

            Log.v(TAG, "Getting output media file");
            mCamera.takePicture(null, null, mPicture);
            //capturePopup will be called inside picture callback
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

    public void showCapturePopup() {
        selectedString = capturedString;
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
        cap_displayView.setTextSize(textSizeConfig.get(textSize));
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
            showCapturePopup();
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
            showCapturePopup();
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
        spk_displayView.setTextSize(textSizeConfig.get(textSize));
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
            showCapturePopup();
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

    //this will recognize text from passed bitmap  TODO: split string by non alphabet
    private void constructText(Bitmap b) {

        SparseArray<TextBlock> items = textRecognizer.detect(new Frame.Builder().setBitmap(b).build());
        if (items.size() != 0) {
            capturedString=getTextAsItIs(items);
//            capturedString=getUniqueStrings(items);
        } else {
            capturedString = "";
        }


        Log.d("detectedString", capturedString);
        showCapturePopup();
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

    private String getSelectedString(TextView view) {
        String s = view.getText().subSequence(view.getSelectionStart(), view.getSelectionEnd()).toString();
        if (s.equals("")) {
            return view.getText().toString();
        }
        return s;
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

    private void copyToClipboard() {
//        selectedString = getSelectedString(cap_displayView);
        ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData myClip = ClipData.newPlainText("text", selectedString);
        myClipboard.setPrimaryClip(myClip);

        Toast.makeText(getApplicationContext(), "Text Copied", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }

    public  String getUniqueStrings(SparseArray<TextBlock> items){
        Set<String> detections=new TreeSet<String>();
//        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            TextBlock item = items.valueAt(i);
            Set<String> detection=new TreeSet<String>(Arrays.asList(item.getValue().replaceAll("[^a-zA-Z]", " ").split("\\s+")));
            Log.d("NIlaan",detection.toString());
            Set<String> finalDetections = detections;
            Set<String> newDetections=new HashSet<String>() {{
                addAll(detection);
                addAll(finalDetections);
            }};
            detections=newDetections;
//                    stringBuilder.append(String.join("\n",item.getValue().split("\\W+")));  //removing all non alphabets
//                    stringBuilder.append("\n");
        }
        Set<String> finalString=new TreeSet<>(detections);
        System.out.println(detections);
        Log.d("NIlaan",detections.toString());
        return String.join("\n",finalString);
    }
    public  String getTextAsItIs(SparseArray<TextBlock> items){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            TextBlock item = items.valueAt(i);
            stringBuilder.append(item.getValue().replaceAll("[^a-zA-Z]", " ").replaceAll("\\s+","  "));
        }
        return stringBuilder.toString();
    }


}