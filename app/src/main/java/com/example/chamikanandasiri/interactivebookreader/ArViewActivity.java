package com.example.chamikanandasiri.interactivebookreader;

import android.content.Intent;
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
import com.google.ar.sceneform.assets.RenderableSource;
import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ArViewActivity extends AppCompatActivity {
    private ModelAnimator modelAnimator;
    private int i;
    private String isbn;
    private ArFragment arFragment;
    private ArrayList<Integer> imagesPath = new ArrayList<>();
    private ArrayList<String> namesPath = new ArrayList<>();
    private ArrayList<String> modelNames = new ArrayList<>();

    private ArrayList<File> arImagesPath = new ArrayList<>();
    private ArrayList<String> arName = new ArrayList<>();
    private ArrayList<File> arModel = new ArrayList<>();

    AnchorNode anchorNode;
    BookObject book;
    private ImageButton btnRemove, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_view);
//        this.book=(BookObject)getIntent().getSerializableExtra("book");
        this.isbn=getIntent().getExtras().getString("isbn");
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        btnRemove = findViewById(R.id.remove);
        btnBack = findViewById(R.id.back);
        getImages();
        try {
            loadimage();
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
//        Uri model=Uri.parse("https://poly.googleusercontent.com/downloads/0BnDT3T1wTE/85QOHCZOvov/Mesh_Beagle.gltf");
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            Anchor anchor = hitResult.createAnchor();

            ModelRenderable.builder()
                    .setSource(this,Uri.fromFile(Common.model))
                    .build()
                    .thenAccept(modelRenderable -> addModelToScene(anchor,modelRenderable));

        });


//            ModelRenderable.builder()
//                    .setSource(this, RenderableSource.builder().setSource(this,Common.model,RenderableSource.SourceType.GLTF2).setScale(0.4f).build())
//                    .setRegistryId(Common.model)
//                    .build()
//                    .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable));
//        });
        btnRemove.setOnClickListener(view -> removeAnchorNode(anchorNode));
        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void loadimage() throws  NullPointerException {
//        /data/data/com.example.chamikanandasiri.interactivebookreader/files/9783161484100
        File ar=new File(this.getFilesDir().getAbsolutePath()+"9783161484100","ar");
        Log.d("isbn",isbn);
        Log.d("arpath",ar.getAbsolutePath());
        File img=new File(this.getFilesDir().getAbsolutePath()+"9783161484100","img");
        for(File f:ar.listFiles()){
//            arName.add(f.getName());
            arModel.add(f);
        }
        for(File f:img.listFiles()){
            arImagesPath.add(f);
        }
    }

    private void getImages() {

        imagesPath.add(R.drawable.table);
        imagesPath.add(R.drawable.bookshelf);
        imagesPath.add(R.drawable.bird);
        imagesPath.add(R.drawable.table);
        imagesPath.add(R.drawable.table);
        imagesPath.add(R.drawable.table);

        namesPath.add("Table");
        namesPath.add("BookShelf");
        namesPath.add("Bird");
        namesPath.add("Cat");
        namesPath.add("Dog");
        namesPath.add("nilaan");
        //https://res.cloudinary.com/db2rl2mxy/raw/upload/v1588865438/0a6a06473eb26f4f8242e874ab1f591f7cb0e2c5..sfb
        //https://poly.googleusercontent.com/downloads/0BnDT3T1wTE/85QOHCZOvov/Mesh_Beagle.gltf
        modelNames.add("https://res.cloudinary.com/db2rl2mxy/raw/upload/v1588865438/0a6a06473eb26f4f8242e874ab1f591f7cb0e2c5..sfb");
        modelNames.add("model.sfb");
        modelNames.add("bird.sfb");
        modelNames.add("bird.sfb");
        modelNames.add("table.sfb");
        modelNames.add("https://res.cloudinary.com/db2rl2mxy/raw/upload/v1588865438/0a6a06473eb26f4f8242e874ab1f591f7cb0e2c5..sfb");
        initiateRecyclerView();
    }

    private void initiateRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerviewAdapter adapter = new RecyclerviewAdapter(this,arImagesPath, arModel);
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