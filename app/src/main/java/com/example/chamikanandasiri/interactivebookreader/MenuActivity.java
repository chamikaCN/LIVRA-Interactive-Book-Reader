package com.example.chamikanandasiri.interactivebookreader;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MenuActivity extends AppCompatActivity {

    private String currentTheme;
    private CardView libraryCard, textDetectorCard, savedItemsCard, alphaGameCard, addBookCard, settingsCard;
    private Dialog savedItemPopup, settingsPopup, addBookPopup, complexBarcodePopup,barcodePopup,detailsPopup, contentPopup;;
    private ImageButton str_closeButton, stn_closeButton, abk_closeButton, cbd_closeButton, brc_closeButton, dtl_closeButton, cnt_closeButton, cnt_backButton;
    private Button str_commentButton, str_wordButton, stn_applyButton, abk_barcodeButton, abk_searchBookButton, cbd_searchButton, brc_detailsButton, brc_loadButton, dtl_contentButton, cnt_downloadButton;
    private SeekBar pitchBar, speedBar;
    private ImageView dtl_imageView;
    private Switch stn_themeSwitch;
    private TextView brc_displayView,dtl_othersView,dtl_titleView;
    private ListView cnt_contentListView;
    private SurfaceView cbd_surfaceView;


    private String detectedISBN;
    private float speechSpeedValue, speechPitchValue;
    private ArrayList<SimpleBookObject> displayingBooks;
    private JSONObject detailsResponse;
    private BookObject book;

    private String TAG = "Test";

    private CameraSource cameraSource;
    private ToneGenerator toneGenerator;
    private BarcodeDetector barcodeDetector;
    private SharedPreferences sharedPreferences;
    private DataBaseHelper dataBaseHelper;
    private BookHandler bookHandler;
    ContentHandler contentHandler;
    final int RequestCameraPermissionID = 1001;

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cbd_surfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = this.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        currentTheme = sharedPreferences.getString("Theme", "Light");
        speechSpeedValue = sharedPreferences.getFloat("Speed", 0.5f);
        speechPitchValue = sharedPreferences.getFloat("Pitch", 0.5f);
        if (currentTheme.equals("Light")) {
            setTheme(R.style.LightTheme);
        } else if (currentTheme.equals("Dark")) {
            setTheme(R.style.DarkTheme);
        }
        setContentView(R.layout.activity_menu);

        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).build();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        dataBaseHelper = new DataBaseHelper(this);
        bookHandler = new BookHandler(dataBaseHelper, this);
        contentHandler = new ContentHandler(dataBaseHelper,this);
        displayingBooks = new ArrayList<>();

        savedItemPopup = new Dialog(this);
        savedItemPopup.setContentView(R.layout.popup_storage);
        settingsPopup = new Dialog(this);
        settingsPopup.setContentView(R.layout.popup_settings);
        addBookPopup = new Dialog(this);
        addBookPopup.setContentView(R.layout.popup_addbook);
        complexBarcodePopup = new Dialog(this);
        complexBarcodePopup.setContentView(R.layout.popup_compexbarcode);
        barcodePopup = new Dialog(this);
        barcodePopup.setContentView(R.layout.popup_barcode);
        detailsPopup = new Dialog(this);
        detailsPopup.setContentView(R.layout.popup_details);
        contentPopup = new Dialog(this);
        contentPopup.setContentView(R.layout.popup_content);

        setupMainLayout();
        setupSavedItemPopup();
        setupAddBookPopup();
        setupSettingsPopup();
        setupComplexBarcodePopup();
        setupBarcodePopup();
        setupDetailsPopup();
        setupContentPopup();
        loadRecentBooks();
    }

    private void setupMainLayout() {
        libraryCard = findViewById(R.id.LibraryCard);
        textDetectorCard = findViewById(R.id.TextDetectorCard);
        savedItemsCard = findViewById(R.id.SavedItemsCard);
        alphaGameCard = findViewById(R.id.AlphaGameCard);
        addBookCard = findViewById(R.id.AddBookCard);
        settingsCard = findViewById(R.id.SettingsCard);

        libraryCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, LibraryActivity.class);
            startActivity(intent);
        });

        textDetectorCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, TextDetectionActivity.class);
            startActivity(intent);
        });

        savedItemsCard.setOnClickListener(this::showSavedItemPopup);

        alphaGameCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);
            startActivity(intent);
        });

        settingsCard.setOnClickListener(this::showSettingsPopup);

        addBookCard.setOnClickListener(v -> showComplexBarcodePopup(v));
    }

    private void setupSavedItemPopup() {
        str_closeButton = savedItemPopup.findViewById(R.id.StorageCloseButton);
        str_commentButton = savedItemPopup.findViewById(R.id.CommentLoadButton);
        str_wordButton = savedItemPopup.findViewById(R.id.WordLoadButton);
    }

    private void setupAddBookPopup() {
        abk_closeButton = addBookPopup.findViewById(R.id.AddBookCloseButton);
        abk_barcodeButton = addBookPopup.findViewById(R.id.AddBookBarcodeButton);
        abk_searchBookButton = addBookPopup.findViewById(R.id.AddBookSearchButton);
    }

    private void setupComplexBarcodePopup() {
        cbd_closeButton = complexBarcodePopup.findViewById(R.id.ComplexBarcodeCloseButton);
        cbd_searchButton = complexBarcodePopup.findViewById(R.id.ComplexBarcodeSearchButton);
        cbd_surfaceView = complexBarcodePopup.findViewById(R.id.ComplexBarcodeSurface);
    }

    private void setupSettingsPopup() {
        stn_closeButton = settingsPopup.findViewById(R.id.SettingsCloseButton);
        stn_themeSwitch = settingsPopup.findViewById(R.id.SettingsThemeSwitch);
        stn_applyButton = settingsPopup.findViewById(R.id.SettingsApplyButton);
        speedBar = settingsPopup.findViewById(R.id.SpeedSeekBar);
        pitchBar = settingsPopup.findViewById(R.id.PitchSeekBar);
    }

    private void setupBarcodePopup() {
        brc_closeButton = barcodePopup.findViewById(R.id.BarcodeCloseButton);
        brc_detailsButton = barcodePopup.findViewById(R.id.BarcodeDetailsButton);
        brc_loadButton = barcodePopup.findViewById(R.id.BarcodeActivityButton);
        brc_displayView = barcodePopup.findViewById(R.id.BarcodeTextView);
        brc_loadButton.setVisibility(View.GONE);
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

    public void showSavedItemPopup(View v1) {
        str_wordButton.setOnClickListener(v2 -> loadStorageActivity("word"));
        str_commentButton.setOnClickListener(v2 -> loadStorageActivity("comment"));
        str_closeButton.setOnClickListener(v2 -> savedItemPopup.dismiss());
        savedItemPopup.show();
    }

    public void showAddBookPopup() {
        abk_closeButton.setOnClickListener(v2 -> addBookPopup.dismiss());
        abk_barcodeButton.setOnClickListener(this::showComplexBarcodePopup);
        addBookPopup.show();
    }

    public void showSettingsPopup(View v) {
        if (currentTheme.equals("Dark")) {
            stn_themeSwitch.setChecked(true);
        } else {
            stn_themeSwitch.setChecked(false);
        }
        pitchBar.setProgress(Math.round(speechPitchValue * 50), true);
        speedBar.setProgress(Math.round(speechSpeedValue * 50), true);
        stn_closeButton.setOnClickListener(v1 -> settingsPopup.dismiss());
        stn_applyButton.setOnClickListener(v1 -> {
            applySettings();
            settingsPopup.dismiss();
        });
        settingsPopup.show();
    }

    public void showComplexBarcodePopup(View v2) {
        detectBarcode();
        barCodeRecognize();
        cbd_searchButton.setOnClickListener(v -> {
        });

        cbd_closeButton.setOnClickListener(v -> {
            complexBarcodePopup.dismiss();
            detectedISBN = "";
        });
        complexBarcodePopup.show();
    }

    public void showBarcodePopup() {
        boolean bookInLibrary = checkBarcodeAlreadyInLibrary(detectedISBN);
        brc_detailsButton.setOnClickListener(v -> {
            dtl_imageView.setImageDrawable(null);
            dtl_titleView.setText("");
            dtl_othersView.setText("");
            showDetailsPopup(v);
            barcodePopup.dismiss();
        });
        brc_loadButton.setOnClickListener(v -> {
            loadARActivity(bookHandler.getBookIDByISBN(detectedISBN));
            barcodePopup.dismiss();
        });
        if (bookInLibrary) {
            String s = detectedISBN + "\n(The book with " + detectedISBN + " code is already in the library)";
            brc_displayView.setText(s);
            brc_loadButton.setVisibility(View.VISIBLE);

        } else {
            brc_loadButton.setVisibility(View.GONE);
            brc_displayView.setText(detectedISBN);
        }
        brc_closeButton.setOnClickListener(v -> {
            barcodePopup.dismiss();
            detectedISBN = "";
        });
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

    public void showContentPopup(String bookID) {
        loadContentDetails(bookID);
        cnt_backButton.setOnClickListener(v1 -> {
            contentPopup.dismiss();
            showDetailsPopup(v1);
        });
        cnt_closeButton.setOnClickListener(v1 -> {
            detectedISBN = "";
            detailsPopup.dismiss();
            contentPopup.dismiss();
        });
        contentPopup.show();
    }

    private void detectBarcode() {

        //creating a video stream through camera
        cameraSource = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(350, 350)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build();
        cbd_surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MenuActivity.this,
                                new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionID);
                        return;
                    }
                    cameraSource.start(cbd_surfaceView.getHolder());
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
                    if (barcode.length() == 10 || (barcode.length() == 13 && barcode.substring(0, 3).equals("978"))) {
                        if (!barcode.equals(detectedISBN)) {
                            toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 350);
                            detectedISBN = barcode;
                            new Handler(Looper.getMainLooper()).post(() -> {
                                complexBarcodePopup.dismiss();
                                showBarcodePopup();
                            });
                        }
                    }
                }
            }
        });

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
                    String s = "Cannot Connect to Server";
                    dtl_othersView.setText(s);
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
        ArrayList<DownloadContentObject> downloadContentDetails;

        if (detailsResponse.has("title")) {
            Log.d(TAG, "formatDetailsResponse: " + message);
            downloadContentDetails = new ArrayList<>();
            String bookID = detailsResponse.getString("id");
            String title = detailsResponse.getString("title");
            String[] isbns = JSONArrayToStringArray(detailsResponse.getJSONArray("isbns"));
            String[] authors = JSONArrayToStringArray(detailsResponse.getJSONArray("authors"));
            String publisherID = detailsResponse.getJSONObject("publisher").getString("id");
            String publisherName = detailsResponse.getJSONObject("publisher").getString("name");
            String[] covers = JSONArrayToStringArray(detailsResponse.getJSONArray("covers"));
            boolean active = detailsResponse.getBoolean("active");
            JSONArray content = detailsResponse.getJSONArray("content");
            int contentAmount = content.length();
            for (int i = 0; i < contentAmount; i++) {
                String[] im = JSONArrayToStringArray(content.getJSONObject(i).getJSONArray("images"));
                String ti = content.getJSONObject(i).getString("title");
                String si = content.getJSONObject(i).getString("size");
                float size1 = Float.parseFloat(si);
                float sizeInKB = size1 / 1024;
                String size;
                if (sizeInKB > 99.99) {
                    float sizeInMB = sizeInKB / 1024;
                    size = String.format("%.2f", sizeInMB) + " MB";
                } else {
                    size = String.format("%.2f", sizeInKB) + " KB";
                }
                String id = content.getJSONObject(i).getString("id");
                String fi = content.getJSONObject(i).getString("file");
                String des = content.getJSONObject(i).getString("description");
                downloadContentDetails.add(new DownloadContentObject(id, im, ti, bookID, des, size, fi, i));
            }
            book = new BookObject(bookID, title, authors, isbns, covers, active, downloadContentDetails, publisherID, publisherName);

            StringBuilder other = new StringBuilder();
            other.append("Author : ").append(authors[0]).append("\nPublisher : ").append(publisherName).append("\nISBN : ").append(isbns[0]);
            if (contentAmount == 0) {
                other.append("\n\nNo AR content is available for this book");
                dtl_contentButton.setBackgroundResource(R.drawable.rounded_button_disabled);
            } else {
                other.append("\n\n").append(contentAmount).append(" models are available");
                dtl_contentButton.setBackgroundResource(R.drawable.rounded_button_three);
                dtl_contentButton.setOnClickListener(v2 -> {
                    showContentPopup(bookID);
                    detailsPopup.dismiss();
                });
            }
            dtl_titleView.setText(title);
            dtl_othersView.setText(other);

            new Handler(Looper.getMainLooper()).post(() -> loadImage(covers[0], dtl_imageView));

        } else {
            String s = "No Details Available for the Book on Our Database";
            dtl_othersView.setText(s);
        }
    }

    private boolean checkBarcodeAlreadyInLibrary(String ISBN) {
        String databaseResult = bookHandler.getBookIDByISBN(ISBN);
        return !databaseResult.equals("empty");
        //Returned book ID can be used later
    }

    private void applySettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        float speed = speedBar.getProgress() / 50f;
        float pitch = pitchBar.getProgress() / 50f;
        boolean themeSwitchValue = stn_themeSwitch.isChecked();
        boolean themechanged = false;
        if ((themeSwitchValue && (currentTheme.equals("Light")))) {
            editor.putString("Theme", "Dark");
            themechanged = true;
        } else if (!themeSwitchValue && (currentTheme.equals("Dark"))) {
            editor.putString("Theme", "Light");
            themechanged = true;
        }
        if (speechPitchValue != pitch) {
            speechPitchValue = pitch;
            editor.putFloat("Pitch", pitch);
        }
        if (speechSpeedValue != speed) {
            speechSpeedValue = speed;
            editor.putFloat("Speed", speed);
        }
        editor.apply();
        if (themechanged) {
            recreate();
        }
    }

    private void loadContentDetails(String bookID) {
        ArrayList<String> databaseContents = contentHandler.getContentIDsByBookIDs(bookID);
        DownloadContentArrayAdapter adapter = new DownloadContentArrayAdapter(this, R.layout.listitem_content, book.getDownloadContent(), databaseContents);
        cnt_contentListView.setAdapter(adapter);
        cnt_downloadButton.setOnClickListener(v -> {
            saveBook(book);
            buttonAnimation4(cnt_downloadButton);
            ArrayList<DownloadContentObject> selected = adapter.getSelectedObjects();
            File bookFile = makeDir(book.getBookId());
            for (DownloadContentObject d : selected) {
                ContentDownloader cd = new ContentDownloader(this, d, bookFile);
                cd.execute();
                contentHandler.addContent(d);
                Log.d("Test", d.getContName());
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

    private void loadRecentBooks() {
        ArrayList<String> bookIDs = bookHandler.getRecentBookIDs();
        for (String id : bookIDs) {
            ArrayList<String> bookdet = bookHandler.getBooksByID(id);
            String title = bookdet.get(0);
            String auth = bookdet.get(1);
            String isbn = bookdet.get(2);
            String img = bookdet.get(3);
            displayingBooks.add(new SimpleBookObject(id, title.toUpperCase(), auth, isbn, img));
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.MenuRecentBookView);
        recyclerView.setLayoutManager(layoutManager);
        RecentBookAdapter adapter = new RecentBookAdapter(this, displayingBooks);
        recyclerView.setAdapter(adapter);
    }

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

    private void saveBook(BookObject book) {
        bookHandler.addBook(book);
    }

    public File makeDir(String fileName) {
        File f = new File(this.getFilesDir(), fileName);
        if (!f.exists()) {
            f.mkdir();
            File ar = new File(f, "ar");
            ar.mkdir();
        }
        return f;
    }

    private void buttonAnimation4(Button button) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        BounceInterpolator bi = new BounceInterpolator(0.2, 20);
        animation.setInterpolator(bi);
        button.startAnimation(animation);
    }

    private void loadStorageActivity(String type) {
        Intent intent = new Intent(this, StorageActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }

    private void loadARActivity(String bookID) {
        Intent intent = new Intent(this, ArViewActivity.class);
        intent.putExtra("booID", bookID);
        startActivity(intent);
    }


}
