package com.example.chamikanandasiri.interactivebookreader;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
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
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

    ImageButton main_captureButton, main_arButton, main_storageButton, main_libraryButton, main_barcodeButton, main_gameButton, cap_dictionaryButton, cap_speechButton, cap_copyButton, cap_commentButton,
            cap_closeButton, spk_speakButton, spk_stopButton, spk_pauseButton, spk_closeButton, spk_backButton, dict_closeButton, dict_backButton,
            dict_saveButton, cmt_backButton, cmt_closeButton, cmt_saveButton, str_closeButton, brc_closeButton, dtl_closeButton, cnt_closeButton, cnt_backButton;

    Button str_wordButton, str_commentButton, brc_detailsButton, dtl_contentButton, cnt_downloadButton;
    SurfaceView cameraView;
    CameraSource cameraSource;

    TextRecognizer textRecognizer;
    BarcodeDetector barcodeDetector;
    MultiDetector multiDetector;
    ToneGenerator toneGenerator;

    ListView cnt_contentListView;
    ImageView dtl_imageView;
    TextView main_detectedView, cap_displayView, spk_displayView, dict_displayView, dict_wordView, cmt_displayView, brc_displayView, dtl_titleView, dtl_othersView;
    SeekBar speedBar, pitchBar;
    EditText cmt_editText, cmt_titleText;
    String detectedString, capturedString, selectedString, searchedWord, detectedISBN;
    JSONObject dictionaryResponse, detailsResponse;
    JSONArray searchResultDefinitions;
    Dialog capturePopup, speechPopup, dictionaryPopup, commentPopup, storagePopup, barcodePopup, detailsPopup, contentPopup;

    DataBaseHelper dbHelper;
    CommentHandler commentHandler;
    WordHandler wordHandler;
    BookHandler bookHandler;
    Speaker speaker;

    int timeStampUniqueCount = 0;

    BookObject book;

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
        setContentView(R.layout.activity_main);

        dbHelper = new DataBaseHelper(this);
        commentHandler = new CommentHandler(dbHelper, this);
        wordHandler = new WordHandler(dbHelper, this);
        bookHandler = new BookHandler(dbHelper, this);
        speaker = new Speaker(this);

        setupMainLayout();
        setupAllPopups();

        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).build();
        multiDetector = new MultiDetector.Builder().add(textRecognizer).add(barcodeDetector).build();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        detectText();
        constructText();
        barCodeRecognize();
        //speechInitialization();

    }

    //UI components Initialization

    private void setupMainLayout() {

        cameraView = findViewById(R.id.Surface);
        main_captureButton = findViewById(R.id.CaptureButton);
        main_arButton = findViewById(R.id.ARButton);
        main_storageButton = findViewById(R.id.StorageButton);
        main_libraryButton = findViewById(R.id.LibraryButton);
        main_barcodeButton = findViewById(R.id.BarcodeButton);
        main_gameButton = findViewById(R.id.GameButton);
        main_detectedView = findViewById(R.id.DetectedTextView);

        main_captureButton.setOnClickListener(v1 -> {
            capturedString = detectedString;
            showCapturePopup(v1);
        });
        main_arButton.setOnClickListener(v -> displayAr());
        main_barcodeButton.setOnClickListener(v2 -> {
            showBarcodePopup(v2);
            main_barcodeButton.clearAnimation();
            main_barcodeButton.setBackground(getResources().getDrawable(R.drawable.rounded_button_disabled));
            main_barcodeButton.setEnabled(false);
        });
        main_storageButton.setOnClickListener(this::showStoragePopup);
        main_libraryButton.setOnClickListener(v2 -> loadLibraryActivity());
        main_gameButton.setOnClickListener(v2 -> loadGameActivity());
        main_barcodeButton.setEnabled(false);
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
        storagePopup = new Dialog(this);
        storagePopup.setContentView(R.layout.popup_storage);
        barcodePopup = new Dialog(this);
        barcodePopup.setContentView(R.layout.popup_barcode);
        detailsPopup = new Dialog(this);
        detailsPopup.setContentView(R.layout.popup_details);
        contentPopup = new Dialog(this);
        contentPopup.setContentView(R.layout.popup_content);

        setupCapturePopup();
        setupSpeechPopup();
        setupDictionaryPopup();
        setupCommentPopup();
        setupStoragePopup();
        setupBarcodePopup();
        setupDetailsPopup();
        setupContentPopup();
    }

    private void setupCapturePopup() {
        cap_dictionaryButton = capturePopup.findViewById(R.id.DictionaryButton);
        cap_speechButton = capturePopup.findViewById(R.id.SpeechButton);
        //cap_copyButton = capturePopup.findViewById(R.id.CopyButton);
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

    private void setupStoragePopup() {
        str_closeButton = storagePopup.findViewById(R.id.StorageCloseButton);
        str_commentButton = storagePopup.findViewById(R.id.CommentLoadButton);
        str_wordButton = storagePopup.findViewById(R.id.WordLoadButton);
    }

    private void setupBarcodePopup() {
        brc_closeButton = barcodePopup.findViewById(R.id.BarcodeCloseButton);
        brc_detailsButton = barcodePopup.findViewById(R.id.BarcodeDetailsButton);
        brc_displayView = barcodePopup.findViewById(R.id.BarcodeTextView);
    }

    private void setupDetailsPopup() {
        dtl_closeButton = detailsPopup.findViewById(R.id.DetailsCloseButton);
        dtl_contentButton = detailsPopup.findViewById(R.id.DetailsContentButton);
        dtl_imageView = detailsPopup.findViewById(R.id.DetailsImageView);
        dtl_othersView = detailsPopup.findViewById(R.id.DetailsOtherTextView);
        dtl_titleView = detailsPopup.findViewById(R.id.DetailsTitleTextView);
    }

    private void setupContentPopup() {
        cnt_closeButton = contentPopup.findViewById(R.id.ContentCloseButton);
        cnt_backButton = contentPopup.findViewById(R.id.ContentBackButton);
        cnt_downloadButton = contentPopup.findViewById(R.id.ContentDownloadButton);
        cnt_contentListView = contentPopup.findViewById(R.id.ContentListView);
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
        //cap_copyButton.setOnClickListener(v1 -> copyToClipboard());
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

    public void showStoragePopup(View v1) {
        str_wordButton.setOnClickListener(v2 -> loadStorageActivity("word"));
        str_commentButton.setOnClickListener(v2 -> loadStorageActivity("comment"));
        str_closeButton.setOnClickListener(v2 -> storagePopup.dismiss());
        storagePopup.show();
    }

    public void showBarcodePopup(View v2) {
        brc_closeButton.setOnClickListener(v -> {
            barcodePopup.dismiss();
//            detectedISBN = "";
        });
        brc_detailsButton.setOnClickListener(v -> {
            dtl_imageView.setImageDrawable(null);
            dtl_titleView.setText("");
            showDetailsPopup(v);
            barcodePopup.dismiss();
        });
        brc_displayView.setText(detectedISBN);
        barcodePopup.show();
    }

    public void showDetailsPopup(View v) {
        getBackendResponse(detectedISBN);
        dtl_closeButton.setOnClickListener(v2 -> {
            detectedISBN = "";
            dtl_titleView.setText("");
            dtl_othersView.setText("");
            dtl_imageView.setImageDrawable(null);
            dtl_contentButton.setOnClickListener(v1 -> {
            });
            detailsPopup.dismiss();
        });
        detailsPopup.show();
    }

    public void showContentPopup(View v2) {
        loadContentDetails();
        cnt_backButton.setOnClickListener(v1 -> {
            contentPopup.dismiss();
            showDetailsPopup(v1);
        });
        cnt_closeButton.setOnClickListener(v1 -> {
            detailsPopup.dismiss();
            contentPopup.dismiss();
        });
        contentPopup.show();
    }

    // Main Functions

    private void detectText() {

        if (!textRecognizer.isOperational()) {
            Log.w("TextDetectionActivity", "Detector dependencies are not yet available");
        } else {
            //creating a video stream through camera
            cameraSource = new CameraSource.Builder(getApplicationContext(), multiDetector)
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
                }else{
                    detectedString = "";
                }
            }
        });
    }

    private void barCodeRecognize() {
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> items = detections.getDetectedItems();
                if (items.size() != 0) {
                    String barcode = items.valueAt(0).displayValue;
                    if (barcode.length() == 10 || barcode.length() == 13) {
                        if (!barcode.equals(detectedISBN)) {
                            toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 350);
                            detectedISBN = barcode;
                            main_barcodeButton.setBackground(getResources().getDrawable(R.drawable.rounded_button_one));
                            buttonAnimation2(main_barcodeButton);
                            main_barcodeButton.setEnabled(true);
                            //TODO deactivate animation, button and set detected string to "" after 5 seconds
                        }
                    }
                }
            }
        });

    }

//    private void speechInitialization() {
//
//        speech = new TextToSpeech(this, status -> {
//            if (status == TextToSpeech.SUCCESS) {
//                int result = speech.setLanguage(Locale.UK);
//                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                    Log.e("TTS", "language not supported");
//                } else {
//                    spk_speakButton.setEnabled(true);
//                }
//            } else {
//                Log.e("TTS", "initializing failed");
//            }
//        }
//        );
//    }

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
            dict_displayView.setText("cannot connect with server");
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
            dict_displayView.setText("No Definitions found");
        }
    }

    private void getBackendResponse(String isbn) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://ar-content-platform-backend.herokuapp.com/api/book/by-isbn/" + isbn)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String message = e.getMessage();
                Log.w("failure Response", message);
                new Handler(Looper.getMainLooper()).post(() -> {
                    dtl_othersView.setText("Cannot Connect to Server");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String message = response.body().string();
                Log.d("Test", message);
                try {
                    formatDetailsResponse(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void formatDetailsResponse(String message) throws JSONException {
        detailsResponse = new JSONObject(message);
        String coverURL;
        ArrayList<DownloadContentObject> downloadContentDetails;

        if (detailsResponse.has("title")) {
            downloadContentDetails = new ArrayList<>();
            String bookID = detailsResponse.getString("id");
            String title = detailsResponse.getString("title");
            String[] isbns = JSONArrayToStringArray(detailsResponse.getJSONArray("isbns"));
            String[] authors = JSONArrayToStringArray(detailsResponse.getJSONArray("authors"));
            String pubid = detailsResponse.getJSONObject("publisher").getString("id");
            String pubname = detailsResponse.getJSONObject("publisher").getString("name");
            String[] covers = JSONArrayToStringArray(detailsResponse.getJSONArray("covers"));
            boolean active = detailsResponse.getBoolean("active");
            JSONArray content = detailsResponse.getJSONArray("content");
            int contentAmount = content.length();
            for (int i = 0; i < contentAmount; i++) {
                String[] im = JSONArrayToStringArray(content.getJSONObject(i).getJSONArray("images"));
                String ti = content.getJSONObject(i).getString("title");
                String si = "2.71MB";
                String id = content.getJSONObject(i).getString("id");
                String fi = content.getJSONObject(i).getString("file");
                String des = content.getJSONObject(i).getString("description");
                downloadContentDetails.add(new DownloadContentObject(id, im, ti, des, si, fi));
            }
            book = new BookObject(bookID, title, authors, isbns, covers, active, downloadContentDetails, pubid, pubname);


            StringBuilder other = new StringBuilder();
            other.append("Author : ").append(authors[0]).append("\nPublisher : ").append(pubname).append("\nISBN : ").append(isbns[0]);
            if (contentAmount == 0) {
                other.append("\n\nNo AR content is available for this book");
                dtl_contentButton.setBackground(getResources().getDrawable(R.drawable.rounded_button_disabled));
            } else {
                other.append("\n\n").append(contentAmount).append(" models are available");
                dtl_contentButton.setBackground(getResources().getDrawable(R.drawable.rounded_button_three));
                dtl_contentButton.setOnClickListener(v2 -> {
                    showContentPopup(v2);
                    detailsPopup.dismiss();
                });
            }
            dtl_titleView.setText(title);
            dtl_othersView.setText(other);

            new Handler(Looper.getMainLooper()).post(() -> loadImage(covers[0], dtl_imageView));

        } else {
            dtl_othersView.setText(" No Details Available for the Book on Our Database");
        }
    }

    //Supporting Functions

    private void speak(String s) {
        float speedValue = (float) speedBar.getProgress() / 50;
        float pitchValue = (float) pitchBar.getProgress() / 50;
        speaker.speak(s,speedValue,pitchValue);
    }

    private void speechPause() {
    }

    private void speechStop(){
        speaker.stop();
    }

    private void saveComment() {
        String phrase = selectedString;
        String title = cmt_titleText.getText().toString();
        String comment = cmt_editText.getText().toString();
        CommentObject commentObject = new CommentObject(title, phrase, comment);
        commentHandler.addComment(commentObject);
    }

    private void saveBook(BookObject book) {
        bookHandler.addBook(book);
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

    private void loadContentDetails() {
        DownloadContentArrayAdapter adapter = new DownloadContentArrayAdapter(this, R.layout.listitem_content, book.getDownloadContent());
        cnt_contentListView.setAdapter(adapter);
        cnt_downloadButton.setOnClickListener(v -> {
            saveBook(book);
            ArrayList<DownloadContentObject> selected = adapter.getSelectedObjects();
            File bookFile=makeDir(book.getIsbns()[0]);
            for (DownloadContentObject d : selected) {
                ContentDownloader cd=new ContentDownloader(this,d,bookFile);
                cd.execute();
                Log.d("Test", d.getContName());
            }
        });

    }

    //Reusable Functions

    private void loadImage(String Url, ImageView im) {
        Picasso.with(this).load(Url)
                .placeholder(R.drawable.ezgif_crop)
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

    //Redirection Functions

    private void loadStorageActivity(String type) {
        Intent intent = new Intent(this, StorageActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }

    private void displayAr() {
        Intent intent = new Intent(this, ArViewActivity.class);
//        intent.putExtra("book",book);
        intent.putExtra("isbn",detectedISBN);
        startActivity(intent);
    }

    private void loadLibraryActivity() {
        Intent intent = new Intent(this, LibraryActivity.class);
        startActivity(intent);
    }

    private void loadGameActivity() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    public void recognizeARActivity() {
        main_arButton.setActivated(true);
        main_arButton.setOnClickListener(v1 -> {
        });
        buttonAnimation2(main_arButton);
    }

    public File makeDir(String fileName){
        File f= new File(this.getFilesDir(),fileName);
        if (!f.exists()){
            f.mkdir();
            File img=new File(f,"img");
            File ar=new File(f,"ar");
            img.mkdir();
            ar.mkdir();
        }

        return f;
    }
}
