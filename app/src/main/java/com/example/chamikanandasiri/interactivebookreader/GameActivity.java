package com.example.chamikanandasiri.interactivebookreader;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private ImageButton gameRestartButton, gameBackButton;
    private Button gameStartButton;
    private ArFragment arFragment;
    private ArrayList<String> models;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.GameFragment);
        gameBackButton = findViewById(R.id.GameBackButton);
        gameRestartButton = findViewById(R.id.GameRestartButton);
        models = new ArrayList<>();
        models.add("A.sfb");
        models.add("B.sfb");
        models.add("C.sfb");
        models.add("D.sfb");

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) ->
        {
            Random random = new Random();
            int ran = random.nextInt(4);
            Anchor anchor = hitResult.createAnchor();
            String model = models.get(ran);
            createModel(anchor,model);

        });
    }

    private void createModel(Anchor anchor,String resource) {
        ModelRenderable.builder()
                .setSource(this, Uri.parse(resource))
                .build()
                .thenAccept(modelRenderable -> placeModel(anchor, modelRenderable));
    }

    private void placeModel(Anchor anchor, ModelRenderable modelRenderable) {
        Log.d("Test", "Place Model");
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();
    }

}

