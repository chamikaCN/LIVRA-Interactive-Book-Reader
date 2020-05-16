package com.example.chamikanandasiri.interactivebookreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ArViewActivity extends AppCompatActivity {
    private ModelAnimator modelAnimator;
    private int i;
    private String bookID;
    private DataBaseHelper dataBaseHelper;
    private ContentHandler contentHandler;
    private ArFragment arFragment;
    private ArrayList<SimpleContentObject> contentAvailable;
    private static File selectedARModel;

    private String TAG = "Test";
    AnchorNode anchorNode;
    String currentTheme;
    private ImageButton btnRemove, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = this.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        currentTheme = sharedPreferences.getString("Theme", "Light");
        if (currentTheme.equals("Light")) {
            setTheme(R.style.LightTheme);
        } else if (currentTheme.equals("Dark")) {
            setTheme(R.style.DarkTheme);
        }
        setContentView(R.layout.activity_ar_view);

        dataBaseHelper = new DataBaseHelper(this);
        contentHandler = new ContentHandler(dataBaseHelper, this);
        this.bookID = getIntent().getExtras().getString("bookID");
        contentAvailable = new ArrayList<>();

        ArrayList<String[]> arrayList = contentHandler.getContentsByBookID(bookID);
        for (String[] a : arrayList) {
            contentAvailable.add(new SimpleContentObject(a[0], a[2], a[1], bookID, a[3]));
        }
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        btnRemove = findViewById(R.id.remove);
        btnBack = findViewById(R.id.back);
        initiateRecyclerView();
        try {
            loadCards();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();
            if (selectedARModel != null) {
                ModelRenderable.builder()
                        .setSource(this, Uri.fromFile(selectedARModel))
                        .build()
                        .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable));
            }
        });
        btnRemove.setOnClickListener(view -> removeAnchorNode(anchorNode));
        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    public static void setSelectedARModel(File f) {
        selectedARModel = f;
    }

    private void loadCards() throws NullPointerException {
        for (SimpleContentObject sc : contentAvailable) {
            File arModel = new File(this.getFilesDir().getAbsolutePath() + "/" + bookID + "/ar/", sc.getContId() + ".sfb");
            sc.setFile(arModel);
            Log.d(TAG, "loadCards: got " + sc.getFile().getName());
        }
    }

    private void initiateRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, contentAvailable);
        recyclerView.setAdapter(adapter);
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {

        anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.getScaleController().setMaxScale(0.02f);
        node.getScaleController().setMinScale(0.01f);
        node.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    public void removeAnchorNode(AnchorNode nodeToremove) {
        if (nodeToremove != null) {
            arFragment.getArSceneView().getScene().removeChild(nodeToremove);
            nodeToremove.getAnchor().detach();
            nodeToremove.setParent(null);
            nodeToremove = null;
        }
    }

    private void animate(ModelRenderable modelRenderable) {
        if (modelAnimator != null && modelAnimator.isRunning())
            modelAnimator.end();
        int animationCount = modelRenderable.getAnimationDataCount();

        if (i == animationCount) {
            i = 0;
        }
        AnimationData animationData = modelRenderable.getAnimationData(i);
        modelAnimator = new ModelAnimator(animationData, modelRenderable);
        modelAnimator.start();
        i++;

    }
}