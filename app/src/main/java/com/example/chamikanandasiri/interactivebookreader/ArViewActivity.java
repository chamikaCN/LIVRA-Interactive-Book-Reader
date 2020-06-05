package com.example.chamikanandasiri.interactivebookreader;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

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
    private VideoRecorder videoRecorder;
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
    private ImageButton btnDeselect, btnBack, btnRotate, btnScaleUp, btnScaleDown, btnTransformModel, btnRemoveModel;
    private Spinner spnAnimation;

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ar_view);

        dataBaseHelper = new DataBaseHelper(this);
        contentHandler = new ContentHandler(dataBaseHelper, this);
        toastManager = new ToastManager(this);
        this.bookID = getIntent().getExtras().getString("bookID");
        contentAvailable = new ArrayList<>();

        ArrayList<String[]> arrayList = contentHandler.getContentsByBookID(bookID);
        for (String[] a : arrayList) {
            contentAvailable.add(new SimpleContentObject(a[0], a[2], a[1], bookID, a[3], (a[4].equals("1"))));
        }
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        scene = arFragment.getArSceneView().getScene();
        btnDeselect = findViewById(R.id.removeButton);
        btnBack = findViewById(R.id.back);
        btnRotate = findViewById(R.id.rotate);
        btnScaleUp = findViewById(R.id.scaleUp);
        spnAnimation = findViewById(R.id.animationSpinner);
        btnTransformModel = findViewById(R.id.transformModel);
        btnScaleDown = findViewById(R.id.scaleDown);
        btnRemoveModel = findViewById(R.id.remove);
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
//                        .setSource(this, Uri.parse("AnimatedDroid.sfb"))
                        .build()
                        .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable));
            } else {
                deselectModel();
                toastManager.showShortToast("Select a card to display model");
            }
        });
        btnTransformModel.setVisibility(View.INVISIBLE);
        btnDeselect.setVisibility(View.GONE);
        btnRotate.setVisibility(View.GONE);
        btnScaleDown.setVisibility(View.GONE);
        btnScaleUp.setVisibility(View.GONE);
        btnRemoveModel.setVisibility(View.INVISIBLE);
        spnAnimation.setVisibility(View.INVISIBLE);

        btnRemoveModel.setOnClickListener(v-> removeAnchorNode());
        btnDeselect.setOnClickListener(view -> {});
        btnBack.setOnClickListener(view -> {
            selectedARModel = null;
            Intent intent = new Intent(this, MenuActivity.class);
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
        if (selectionAnimation != null) {
            if (selectionAnimation.isRunning()) {
                selectionAnimation.end();
            }
        }
        tappedNode = node;
        selectionAnimation = createSelectionAnimator(tappedNode.getLocalScale());
        btnTransformModel.setVisibility(View.VISIBLE);
        btnDeselect.setVisibility(View.VISIBLE);
        btnRemoveModel.setVisibility(View.VISIBLE);
        closeButtonPanel();
        btnTransformModel.setOnClickListener(v -> openButtonPanel());
        spnAnimation.setVisibility(View.INVISIBLE);
        animateModel((ModelRenderable) tappedNode.getRenderable());
        startNewSelectionAnimation();

        btnRotate.setOnClickListener(v -> {
            Quaternion newQuat = Quaternion.axisAngle(new Vector3(0, 1, 0), tappedNode.getLocalRotation().w + 20);
            tappedNode.setLocalRotation(newQuat);
        });

        btnScaleUp.setOnClickListener(v -> {
            Vector3 newVector = Vector3.add(tappedNode.getLocalScale(), new Vector3(0.1f, 0.1f, 0.1f));
            selectionAnimation.end();
            tappedNode.setLocalScale(newVector);
            if (modelAnimator != null) {
                if (!modelAnimator.isRunning()) {
                    startNewSelectionAnimation();
                }
            } else {
                startNewSelectionAnimation();
            }
        });
        btnScaleDown.setOnClickListener(v -> {
            Vector3 newVector = Vector3.add(tappedNode.getLocalScale(), new Vector3(-0.1f, -0.1f, -0.1f));
            selectionAnimation.end();
            tappedNode.setLocalScale(newVector);
            if (modelAnimator != null) {
                if (!modelAnimator.isRunning()) {
                    startNewSelectionAnimation();
                }
            } else {
                startNewSelectionAnimation();
            }
        });
        btnDeselect.setOnClickListener(v-> deselectModel());
    }



    private void deselectModel() {
        if (selectionAnimation != null) {
            if (selectionAnimation.isRunning()) {
                selectionAnimation.end();
            }
        }
        if (modelAnimator != null) {
            if (modelAnimator.isRunning()) {
                modelAnimator.end();
            }
        }
        closeButtonPanel();
        spnAnimation.setVisibility(View.INVISIBLE);
        btnTransformModel.setVisibility(View.INVISIBLE);
        btnRemoveModel.setVisibility(View.INVISIBLE);
        tappedNode = null;
    }

    private void openButtonPanel() {
        btnRotate.setVisibility(View.VISIBLE);
        btnScaleUp.setVisibility(View.VISIBLE);
        btnScaleDown.setVisibility(View.VISIBLE);
        btnDeselect.setVisibility(View.VISIBLE);
        btnTransformModel.setImageResource(R.drawable.ic_ar_panelclose);
        btnTransformModel.setOnClickListener(v -> closeButtonPanel());
    }

    private void closeButtonPanel() {
        btnRotate.setVisibility(View.GONE);
        btnScaleUp.setVisibility(View.GONE);
        btnScaleDown.setVisibility(View.GONE);
        btnDeselect.setVisibility(View.GONE);
        btnTransformModel.setImageResource(R.drawable.ic_transform);
        btnTransformModel.setOnClickListener(v -> openButtonPanel());
    }

    private void startNewSelectionAnimation() {
        selectionAnimation = createSelectionAnimator(tappedNode.getLocalScale());
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
            closeButtonPanel();
            spnAnimation.setVisibility(View.INVISIBLE);
            btnTransformModel.setVisibility(View.INVISIBLE);
            btnRemoveModel.setVisibility(View.INVISIBLE);
            tappedNode = null;
        }
    }

    private void animateModel(ModelRenderable modelRenderable) {
        if (modelAnimator != null && modelAnimator.isRunning())
            modelAnimator.end();

        int animationCount = modelRenderable.getAnimationDataCount();

        if (animationCount > 0) {
            String[] animations = new String[animationCount + 1];
            animations[0] = "None";
            for (int m = 1; m <= animationCount; m++) {
                animations[m] = modelRenderable.getAnimationData(m - 1).getName();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_selectitem, animations);
            adapter.setDropDownViewResource(R.layout.spinner_dropdownitem);
            spnAnimation.setAdapter(adapter);
            spnAnimation.setVisibility(View.VISIBLE);

            spnAnimation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (modelAnimator != null && modelAnimator.isRunning())
                        modelAnimator.end();
                    if (parent.getItemAtPosition(position) != "None") {
                        selectionAnimation.end();
                        String selectedAnim = spnAnimation.getSelectedItem().toString();
                        AnimationData animationData = modelRenderable.getAnimationData(selectedAnim);
                        modelAnimator = new ModelAnimator(animationData, modelRenderable);
                        modelAnimator.start();
                        modelAnimator.setRepeatCount(1000);
                    } else {
                        startNewSelectionAnimation();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    private static ObjectAnimator createSelectionAnimator(Vector3 size) {
        Vector3 v1 = size.scaled(0.97f);
        Vector3 v2 = size;
        Vector3 v3 = size.scaled(1.04f);
        Vector3 v4 = size;
        Vector3 v5 = size.scaled(0.97f);

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