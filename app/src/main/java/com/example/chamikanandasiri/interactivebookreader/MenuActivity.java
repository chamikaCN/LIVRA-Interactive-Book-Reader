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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MenuActivity extends AppCompatActivity {

    private String currentTheme, textSize;
    private CardView libraryCard, textDetectorCard, savedItemsCard, alphaGameCard, addBookCard, settingsCard;
    private Dialog savedItemPopup, settingsPopup, complexBarcodePopup, searchBookPopup, barcodePopup, detailsPopup, contentPopup;
    private ImageButton str_closeButton, stn_closeButton, stn_textPlusButton, stn_textMinusButton, cbd_closeButton, sbk_closeButton, sbk_searchButton, brc_closeButton, dtl_closeButton, dtl_backButton, cnt_closeButton, cnt_backButton;
    private Button str_commentButton, str_wordButton, stn_applyButton, cbd_searchButton, sbk_proceedButton, brc_detailsButton, brc_loadButton, dtl_contentButton, cnt_downloadButton;
    private SeekBar pitchBar, speedBar;
    private ImageView dtl_imageView;
    private Switch stn_themeSwitch, stn_voiceSwitch;
    private CheckBox stn_voiceCheckBox;
    private TextView brc_displayView, dtl_othersView, dtl_titleView, stn_textSizeView;
    private ListView cnt_contentListView;
    private SurfaceView cbd_surfaceView;
    private EditText sbk_searchText;

    private RecentBookAdapter recentBookAdapter;

    private String detectedISBN;
    private float speechSpeedValue, speechPitchValue;
    private ArrayList<SimpleBookObject> displayingRecentBooks;
    private ArrayList<BookObject> bookResponceObjects;
    private static BookObject book;
    private JSONArray bookResponse;
    private HashMap<String, Integer> textSizeConfig;

    private String TAG = "Test";

    private CameraSource cameraSource;
    private ToneGenerator toneGenerator;
    private BarcodeDetector barcodeDetector;
    private SharedPreferences sharedPreferences;
    private DataBaseHelper dataBaseHelper;
    private BookHandler bookHandler;
    private ToastManager toastManager;
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
        setContentView(R.layout.activity_menu);

        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).build();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        dataBaseHelper = new DataBaseHelper(this);
        bookHandler = new BookHandler(dataBaseHelper, this);
        contentHandler = new ContentHandler(dataBaseHelper, this);
        toastManager = new ToastManager(this);
        displayingRecentBooks = new ArrayList<>();
        bookResponceObjects = new ArrayList<>();

        savedItemPopup = new Dialog(this);
        savedItemPopup.setContentView(R.layout.popup_storage);
        settingsPopup = new Dialog(this);
        settingsPopup.setContentView(R.layout.popup_settings);
        complexBarcodePopup = new Dialog(this);
        complexBarcodePopup.setContentView(R.layout.popup_compexbarcode);
        searchBookPopup = new Dialog(this);
        searchBookPopup.setContentView(R.layout.popup_booksearch);
        barcodePopup = new Dialog(this);
        barcodePopup.setContentView(R.layout.popup_barcode);
        detailsPopup = new Dialog(this);
        detailsPopup.setContentView(R.layout.popup_details);
        contentPopup = new Dialog(this);
        contentPopup.setContentView(R.layout.popup_content);

        setupMainLayout();
        setupSavedItemPopup();
        setupSettingsPopup();
        setupComplexBarcodePopup();
        setupSearchBookPopup();
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
            intent.putExtra("type", "library");
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

        addBookCard.setOnClickListener(this::showComplexBarcodePopup);
    }

    private void setupSavedItemPopup() {
        str_closeButton = savedItemPopup.findViewById(R.id.StorageCloseButton);
        str_commentButton = savedItemPopup.findViewById(R.id.CommentLoadButton);
        str_wordButton = savedItemPopup.findViewById(R.id.WordLoadButton);
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
        stn_textPlusButton = settingsPopup.findViewById(R.id.SettingsPlusButton);
        stn_textMinusButton = settingsPopup.findViewById(R.id.SettingsMinusButton);
        speedBar = settingsPopup.findViewById(R.id.SpeedSeekBar);
        pitchBar = settingsPopup.findViewById(R.id.PitchSeekBar);
        stn_voiceSwitch = settingsPopup.findViewById(R.id.SettingsVoiceSupportSwitch);
        stn_voiceCheckBox = settingsPopup.findViewById(R.id.SettingsVoiceConfigCheckBox);
        stn_textSizeView = settingsPopup.findViewById(R.id.SettingsTextSizeView);
    }

    private void setupSearchBookPopup() {
        sbk_closeButton = searchBookPopup.findViewById(R.id.BookSearchCloseButton);
        sbk_searchButton = searchBookPopup.findViewById(R.id.BookSearchSearchButton);
        sbk_searchText = searchBookPopup.findViewById(R.id.BookSearchEditText);
        sbk_proceedButton = searchBookPopup.findViewById(R.id.BookSearchProceedButton);
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
        dtl_backButton = detailsPopup.findViewById(R.id.DetailsBackButton);
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

    public void showSettingsPopup(View v) {
        if (currentTheme.equals("Dark")) {
            stn_themeSwitch.setChecked(true);
        } else {
            stn_themeSwitch.setChecked(false);
        }
        boolean voice = toastManager.getVoiceSupport();
        stn_voiceSwitch.setChecked(voice);
        stn_voiceCheckBox.setChecked(toastManager.getVoiceSupportConfigurability());
        stn_voiceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                stn_voiceCheckBox.setEnabled(true);
            } else {
                stn_voiceCheckBox.setEnabled(false);
            }
        });
        stn_textSizeView.setText(textSize);
        stn_textMinusButton.setOnClickListener(v1 -> {
            String text = stn_textSizeView.getText().toString();
            if (text.equals("Medium")) {
                stn_textSizeView.setText("Small");
                stn_textMinusButton.setEnabled(false);
            } else if (text.equals("Large")) {
                stn_textSizeView.setText("Medium");
                stn_textPlusButton.setEnabled(true);
            }
        });
        stn_textPlusButton.setOnClickListener(v1 -> {
            String text = stn_textSizeView.getText().toString();
            if (text.equals("Medium")) {
                stn_textSizeView.setText("Large");
                stn_textPlusButton.setEnabled(false);
            } else if (text.equals("Small")) {
                stn_textSizeView.setText("Medium");
                stn_textMinusButton.setEnabled(true);
            }
        });
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
            complexBarcodePopup.dismiss();
            showBookSearchPopup();
        });

        cbd_closeButton.setOnClickListener(v -> {
            complexBarcodePopup.dismiss();
            detectedISBN = "";
        });
        complexBarcodePopup.show();
    }

    public void showBookSearchPopup() {
        sbk_closeButton.setOnClickListener(v -> {
            searchBookPopup.dismiss();
            if (!bookResponceObjects.isEmpty()) {
                bookResponceObjects.clear();
            }
            sbk_searchText.setText("");
        });
        sbk_searchButton.setOnClickListener(v -> {
            searchDatabase();
            buttonAnimation1(sbk_searchButton);
        });
        sbk_proceedButton.setOnClickListener(v -> {
            if (book != null) {
                searchBookPopup.dismiss();
                buildDetailsPopupWithBook();
                dtl_backButton.setVisibility(View.VISIBLE);
                dtl_contentButton.setVisibility(View.GONE);
                showDetailsPopup(v);
            } else {
                toastManager.showShortToast("You should select a book first");
            }
        });
        searchBookPopup.show();
    }

    public void showBarcodePopup() {
        boolean bookInLibrary = checkBarcodeAlreadyInLibrary(detectedISBN);
        brc_detailsButton.setOnClickListener(v -> {
            dtl_imageView.setImageDrawable(null);
            dtl_titleView.setText("");
            dtl_othersView.setText("");
            getBackendResponse(detectedISBN);
            dtl_backButton.setVisibility(View.GONE);
            dtl_contentButton.setVisibility(View.GONE);
            showDetailsPopup(v);
            barcodePopup.dismiss();
        });
        brc_loadButton.setOnClickListener(v -> {
            Log.d(TAG, "showBarcodePopup: " + bookHandler.getBookIDByISBN(detectedISBN));
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
        dtl_closeButton.setOnClickListener(v2 -> {
            detectedISBN = "";
            book = null;
            if (!bookResponceObjects.isEmpty()) {
                bookResponceObjects.clear();
            }
            sbk_searchText.setText("");
            dtl_titleView.setText("");
            dtl_othersView.setText("");
            dtl_imageView.setImageDrawable(null);
            dtl_contentButton.setOnClickListener(v1 -> {
            });
            detailsPopup.dismiss();
        });
        dtl_backButton.setOnClickListener(v2 -> {
            dtl_titleView.setText("");
            dtl_othersView.setText("");
            dtl_imageView.setImageDrawable(null);
            dtl_contentButton.setOnClickListener(v1 -> {
            });
            showBookSearchPopup();
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
            book = null;
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
                    toastManager.showShortToast((message.equals("timeout")) ? "Request timed out. Please Try Again!!" : "Cannot connect to server");
                    String s = "Cannot Connect to Server";
                    dtl_othersView.setText(s);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String message = response.body().string();
                Log.d("Test", message);
                try {
                    formatDetailsResponse(new JSONObject(message));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void searchDatabase() {
        String searchString = sbk_searchText.getText().toString();
        String json = "{\"title\":\"" + searchString + "\"}";

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        if (searchString.length() > 1) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://ar-content-platform-backend.herokuapp.com/api/book/by-title/")
                    .post(body)
                    .addHeader("Cache-Control", "no-cache")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    String message = e.getMessage();
                    toastManager.showShortToast((message.equals("timeout")) ? "Request timed out. Please Try Again!!" : "Cannot connect to server");
                    Log.d(TAG, "fail" + message);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String message = response.body().string();
                    Log.d(TAG, "okay" + message);
                    try {
                        formatDatabaseResponse(new JSONArray(message));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Keyword should be at least 2 characters long", Toast.LENGTH_SHORT).show();
        }
    }

    private void formatDatabaseResponse(JSONArray array) throws JSONException {
        bookResponse = array;
        bookResponceObjects.clear();
        if (bookResponse.length() > 0) {
            for (int k = 0; k < bookResponse.length(); k++) {
                JSONObject obj = bookResponse.getJSONObject(k);
                BookObject bookObject = JsonBookResponseToBookObject(obj);
                if (bookObject != null) {
                    bookResponceObjects.add(bookObject);
                }
            }
        } else {
            toastManager.showLongToast("No matching books found in our database");
        }
        new Handler(Looper.getMainLooper()).post(() -> loadBookSearchResultGrid(bookResponceObjects));
    }

    private void loadBookSearchResultGrid(ArrayList<BookObject> books) {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        RecyclerView recyclerView = searchBookPopup.findViewById(R.id.BookSearchGridView);
        recyclerView.setLayoutManager(layoutManager);
        BookArrayAdapter adapter = new BookArrayAdapter(this, books);
        recyclerView.setAdapter(adapter);
    }

    private void formatDetailsResponse(JSONObject obj) throws JSONException {
        BookObject responseBook = JsonBookResponseToBookObject(obj);

        if (responseBook != null) {
            setBook(responseBook);
            new Handler(Looper.getMainLooper()).post(this::buildDetailsPopupWithBook);
        } else {
            String s = "No Details Available for the Book on Our Database";
            dtl_othersView.setText(s);
        }
    }

    private void buildDetailsPopupWithBook() {
        StringBuilder other = new StringBuilder();
        other.append("Author : ").append(book.getAuthors()[0]).append("\nPublisher : ").append(book.getPublisherName()).append("\nISBN : ").append(book.getIsbns()[0]);
        int availableContentAmount = book.getDownloadContent().size();
        if (availableContentAmount == 0) {
            other.append("\n\nNo AR content is available for this book");
            dtl_contentButton.setBackgroundResource(R.drawable.rounded_button_disabled);
        } else {
            other.append("\n\n").append(availableContentAmount).append(" models are available");
            dtl_contentButton.setBackgroundResource(R.drawable.rounded_button_three);
            dtl_contentButton.setOnClickListener(v2 -> {
                showContentPopup(book.getBookId());
                detailsPopup.dismiss();
            });
        }
        loadImage(book.getCovers()[0], dtl_imageView);
        dtl_titleView.setText(book.getTitle());
        dtl_othersView.setText(other);
        new Handler(Looper.getMainLooper()).post(() -> dtl_contentButton.setVisibility(View.VISIBLE));
    }

    private BookObject JsonBookResponseToBookObject(JSONObject jsonObject) throws JSONException {
        ArrayList<DownloadContentObject> downloadContentDetails;
        BookObject bk = null;
        if (jsonObject.has("title")) {
            downloadContentDetails = new ArrayList<>();
            String bookID = jsonObject.getString("id");
            String title = jsonObject.getString("title");
            String[] isbns = JSONArrayToStringArray(jsonObject.getJSONArray("isbns"));
            String[] authors = JSONArrayToStringArray(jsonObject.getJSONArray("authors"));
            String publisherID = jsonObject.getJSONObject("publisher").getString("id");
            String publisherName = jsonObject.getJSONObject("publisher").getString("name");
            String[] covers = JSONArrayToStringArray(jsonObject.getJSONArray("covers"));
            boolean active = jsonObject.getBoolean("active");
            JSONArray content = jsonObject.getJSONArray("content");
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
                boolean ani = content.getJSONObject(i).getBoolean("animated");

                downloadContentDetails.add(new DownloadContentObject(id, im, ti, bookID, des, size, fi, ani, i));
            }
            bk = (new BookObject(bookID, title, authors, isbns, covers, active, downloadContentDetails, publisherID, publisherName));
        }
        return bk;
    }

    public static void setBook(BookObject newbook) {
        book = newbook;
    }

    private boolean checkBarcodeAlreadyInLibrary(String ISBN) {
        String databaseResult = bookHandler.getBookIDByISBN(ISBN);
        return !databaseResult.equals("empty");
        //Returned book ID can be used later
    }

    private void loadContentDetails(String bookID) {
        ArrayList<String> alreadyDownloadedContents = contentHandler.getContentIDsByBookIDs(bookID);
        DownloadContentArrayAdapter adapter = new DownloadContentArrayAdapter(this, R.layout.listitem_content, book.getDownloadContent(), alreadyDownloadedContents);
        cnt_contentListView.setAdapter(adapter);
        cnt_downloadButton.setOnClickListener(v -> {
            if(adapter.isAnySelected()) {
                saveBook(book);
                loadRecentBooks();
                buttonAnimation4(cnt_downloadButton);
                ArrayList<DownloadContentObject> selected = adapter.getSelectedObjects();
                File bookFile = makeDir(book.getBookId());
                for (DownloadContentObject d : selected) {
                    ContentDownloader cd = new ContentDownloader(this, d, bookFile);
                    cd.execute();
                    contentHandler.addContent(d);
                }
                loadContentDetails(bookID);
            }else{
                toastManager.showShortToast("Select models to Download");
            }
        });
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
        String currentSize = stn_textSizeView.getText().toString();
        if (!currentSize.equals(textSize)) {
            textSize = currentSize;
            editor.putString("TextSize", currentSize);
        }
        boolean voiceConf = stn_voiceCheckBox.isChecked();
        if (voiceConf != toastManager.getVoiceSupportConfigurability()) {
            toastManager.setVoiceSupportConfigurability(voiceConf);
            editor.putBoolean("VoiceConfig", voiceConf);
        }
        boolean voiceSup = stn_voiceSwitch.isChecked();
        if (voiceSup != toastManager.getVoiceSupport()) {
            toastManager.setVoiceSupport(voiceSup);
            editor.putBoolean("VoiceSupport", voiceSup);
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
        displayingRecentBooks.clear();
        ArrayList<String> bookIDs = bookHandler.getRecentBookIDs();
        for (String id : bookIDs) {
            ArrayList<String> bookdet = bookHandler.getBooksByID(id);
            String title = bookdet.get(0);
            String auth = bookdet.get(1);
            String isbn = bookdet.get(2);
            String img = bookdet.get(3);
            displayingRecentBooks.add(new SimpleBookObject(id, title.toUpperCase(), auth, isbn, img));
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.MenuRecentBookView);
        recyclerView.setLayoutManager(layoutManager);
        recentBookAdapter = new RecentBookAdapter(this, displayingRecentBooks);
        recyclerView.setAdapter(recentBookAdapter);
    }

    private void loadImage(String Url, ImageView im) {
        Picasso.with(this).load(Url)
                .placeholder(R.drawable.bookcover_loading_anim)
                .fit()
                .into(im);
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

    private void buttonAnimation1(ImageButton button) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        BounceInterpolator bi = new BounceInterpolator(0.2, 20);
        animation.setInterpolator(bi);
        button.startAnimation(animation);
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
        Log.d(TAG, "loadARActivity: cat");
        Intent intent = new Intent(this, ArViewActivity.class);
        intent.putExtra("bookID", bookID);
        startActivity(intent);
    }
}
