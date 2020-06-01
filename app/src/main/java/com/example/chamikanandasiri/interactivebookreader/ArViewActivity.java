package com.example.chamikanandasiri.interactivebookreader;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ArViewActivity extends AppCompatActivity {
    private ModelAnimator modelAnimator;
    private ObjectAnimator selectionAnimation;
    private int i;
    private String bookID;
    private DataBaseHelper dataBaseHelper;
    private ContentHandler contentHandler;
    private ToastManager toastManager;
    private ArFragment arFragment;
    private Scene scene;
    private Node tappedNode;
    private ArrayList<SimpleContentObject> contentAvailable;
    private static File selectedARModel;

    private String TAG = "Test";
    String currentTheme;
    private ImageButton btnRemove, btnBack, btnRotate, btnScaleUp, btnScaleDown;

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
        toastManager = new ToastManager(this);
        selectionAnimation = createSelectionAnimator();
        this.bookID = getIntent().getExtras().getString("bookID");
        contentAvailable = new ArrayList<>();

        ArrayList<String[]> arrayList = contentHandler.getContentsByBookID(bookID);
        for (String[] a : arrayList) {
            contentAvailable.add(new SimpleContentObject(a[0], a[2], a[1], bookID, a[3]));
        }
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        scene = arFragment.getArSceneView().getScene();
        btnRemove = findViewById(R.id.remove);
        btnBack = findViewById(R.id.back);
        btnRotate = findViewById(R.id.rotate);
        btnScaleUp = findViewById(R.id.scaleUp);
        btnScaleDown = findViewById(R.id.scaleDown);
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
            } else {
                toastManager.showShortToast("Select a card to display model");
            }
        });
        btnRotate.setVisibility(View.GONE);
        btnScaleDown.setVisibility(View.GONE);
        btnScaleUp.setVisibility(View.GONE);
        btnRemove.setOnClickListener(view -> removeAnchorNode());
        btnBack.setOnClickListener(view -> {
            selectedARModel = null;
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        });
        btnRotate.setOnClickListener(v1 -> {
            rotateObject();
            Log.d(TAG, "onCreate: came");
        });
    }

    public static void setSelectedARModel(File f) {
        selectedARModel = f;
    }

    private void loadCards() throws NullPointerException {
        for (SimpleContentObject sc : contentAvailable) {
            File arModel = new File(this.getFilesDir().getAbsolutePath() + "/" + bookID + "/ar/", sc.getContId() + ".sfb");
            sc.setFile(arModel);
        }
    }

    private void initiateRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(layoutManager);
        ArCardViewAdapter adapter = new ArCardViewAdapter(this, contentAvailable);
        recyclerView.setAdapter(adapter);
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setRenderable(modelRenderable);
        scene.addChild(anchorNode);
        setTappedNode(anchorNode);

        anchorNode.setOnTapListener((hitResult, motionEvent) -> {
            if (hitResult.getNode() != null) {
                setTappedNode(hitResult.getNode());
            }
        });
    }

    private void setTappedNode(Node node) {
        tappedNode = node;
        btnRotate.setVisibility(View.VISIBLE);
        btnScaleUp.setVisibility(View.VISIBLE);
        btnScaleDown.setVisibility(View.VISIBLE);
        selectionAnimation.setTarget(tappedNode);
        selectionAnimation.setDuration(2000);
        selectionAnimation.start();
    }

    private void rotateObject() {
        ObjectAnimator rotateAnimation = createOrbitAnimator();
        rotateAnimation.setTarget(tappedNode);
        rotateAnimation.setDuration(1000);
        rotateAnimation.start();
    }

    public void removeAnchorNode() {
        if (tappedNode != null) {
            scene.removeChild(tappedNode);
            btnRotate.setVisibility(View.GONE);
            btnScaleDown.setVisibility(View.GONE);
            btnScaleUp.setVisibility(View.GONE);
            tappedNode = null;
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

    private static ObjectAnimator createSelectionAnimator() {
        Vector3 v1 = new Vector3(0.97f, 0.97f, 0.97f);
        Vector3 v2 = new Vector3(1.0f, 1.0f, 1.0f);
        Vector3 v3 = new Vector3(1.04f, 1.04f, 1.04f);
        Vector3 v4 = new Vector3(1.0f, 1.0f, 1.0f);
        Vector3 v5 = new Vector3(0.97f, 0.97f, 0.97f);

        ObjectAnimator orbitAnimation = new ObjectAnimator();
        orbitAnimation.setObjectValues(v1, v2, v3, v4, v5);
        orbitAnimation.setPropertyName("localScale");
        orbitAnimation.setEvaluator(new Vector3Evaluator());

        orbitAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        orbitAnimation.setRepeatMode(ObjectAnimator.RESTART);
        orbitAnimation.setInterpolator(new LinearInterpolator());
        orbitAnimation.setAutoCancel(true);

        return orbitAnimation;
    }

    private static ObjectAnimator createOrbitAnimator() {

        Quaternion orientation1 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 0.0f);
        Quaternion orientation2 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 90.0f);
        Quaternion orientation4 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 0.0f);

        ObjectAnimator orbitAnimation = new ObjectAnimator();
        orbitAnimation.setObjectValues(orientation1, orientation2, orientation4);
        orbitAnimation.setPropertyName("localRotation");
        orbitAnimation.setEvaluator(new QuaternionEvaluator());

        orbitAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        orbitAnimation.setRepeatMode(ObjectAnimator.RESTART);
        orbitAnimation.setInterpolator(new LinearInterpolator());
        orbitAnimation.setAutoCancel(true);

        return orbitAnimation;
    }
}