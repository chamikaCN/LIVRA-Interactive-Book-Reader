package com.example.chamikanandasiri.interactivebookreader;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private ImageButton gameRestartButton, gameBackButton, gameRemoveButton, letterCloseButton;
    private Button gameStartButton, gameChangeButton, gameCheckButton, letterRandomButton, letterOkButton, endRetryButton, endBackButton;
    private TextView gameLetterView, gameDetailsView, endMessageView;
    private EditText letterEditText;
    private Speaker speaker;
    private CustomARFragment arFragment;
    private ArrayList<String> models, generatedLetters;
    private ArrayList<Node> generatedNodes;
    private Scene scene;
    private String selectedLetter, givenLetter;
    private Node selectedNode, tappedNode;
    private Camera camera;
    private HashMap<Node, String> nodeStringPairs;
    private Dialog letterPopup, endPopup;
    private int modelsRendered, gameSize = 6, score = 0;
    private String TAG = "Test";
    String currentTheme;

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

        setContentView(R.layout.activity_game);

        arFragment = (CustomARFragment) getSupportFragmentManager().findFragmentById(R.id.GameFragment);
        gameBackButton = findViewById(R.id.GameBackButton);
        gameRestartButton = findViewById(R.id.GameRestartButton);
        gameRemoveButton = findViewById(R.id.GameRemoveButton);
        gameStartButton = findViewById(R.id.GameStartButton);
        gameChangeButton = findViewById(R.id.GameChangeButton);
        gameCheckButton = findViewById(R.id.GameCheckButton);
        gameCheckButton.setVisibility(View.GONE);
        gameLetterView = findViewById(R.id.GameLetterView);
        gameDetailsView = findViewById(R.id.GameDisplayView);

        letterPopup = new Dialog(this);
        letterPopup.setContentView(R.layout.popup_letterselect);
        letterCloseButton = letterPopup.findViewById(R.id.LetterSelectCloseButton);
        letterOkButton = letterPopup.findViewById(R.id.LetterSelectOKButton);
        letterRandomButton = letterPopup.findViewById(R.id.LetterSelectRandomButton);
        letterEditText = letterPopup.findViewById(R.id.LetterSelectEditText);

        endPopup = new Dialog(this);
        endPopup.setContentView(R.layout.popup_endgame);
        endBackButton = endPopup.findViewById(R.id.EndgameBackButton);
        endRetryButton = endPopup.findViewById(R.id.EndgameRetryButton);
        endMessageView = endPopup.findViewById(R.id.EndGameMessage);

        speaker = new Speaker(this);
        models = new ArrayList<>();
        generatedNodes = new ArrayList<>();
        generatedLetters = new ArrayList<>();
        nodeStringPairs = new HashMap<>();
        scene = arFragment.getArSceneView().getScene();
        camera = scene.getCamera();
        selectedLetter = null;
        addAlphabet();

        arFragment.getPlaneDiscoveryController().show();

        setLetterOnTapGeneration();


        gameBackButton.setOnClickListener(v1 -> loadMainActivity());
        gameRestartButton.setOnClickListener(v1 -> reset());
        gameRemoveButton.setOnClickListener(v1 -> remove());
        gameStartButton.setOnClickListener(v1 -> startGame());
        gameChangeButton.setOnClickListener(this::showLetterPopup);
    }

    private void setLetterOnTapGeneration() {
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) ->
        {
            String model;
            Anchor anchor = hitResult.createAnchor();
            if (selectedLetter == null) {
                model = getRandomLetterModel();
            } else {
                model = selectedLetter + ".sfb";
            }
            speaker.speak(model.substring(0, 1), 0.5f, 0.5f);
            createModel(anchor, model);
        });
    }

    public void showLetterPopup(View v1) {
        letterCloseButton.setOnClickListener(v2 -> letterPopup.dismiss());
        letterRandomButton.setOnClickListener(v2 -> {
            selectedLetter = null;
            gameLetterView.setText("Random");
            letterPopup.dismiss();
        });
        letterOkButton.setOnClickListener(v -> {
            if (Pattern.matches("[a-zA-Z]", letterEditText.getText().toString())) {
                selectedLetter = letterEditText.getText().toString().toUpperCase();
                gameLetterView.setText(selectedLetter);
            }
            letterPopup.dismiss();
        });
        letterPopup.show();
    }

    public void showEndPopup(String mes) {
        endRetryButton.setOnClickListener(v2 -> {
            startGame();
            endPopup.dismiss();
        });
        endBackButton.setOnClickListener(v2 -> {
            gameChangeButton.setVisibility(View.VISIBLE);
            gameRestartButton.setVisibility(View.VISIBLE);
            gameRemoveButton.setVisibility(View.VISIBLE);
            gameStartButton.setVisibility(View.VISIBLE);
            gameDetailsView.setVisibility(View.VISIBLE);
            gameLetterView.setText("Random");
            selectedLetter = null;
            gameCheckButton.setVisibility(View.GONE);
            arFragment.getPlaneDiscoveryController().show();
            setLetterOnTapGeneration();
            endPopup.dismiss();
        });
        endMessageView.setText(mes);
        endPopup.show();
    }

    private void addAlphabet() {
        models.add("A.sfb");
        models.add("B.sfb");
        models.add("C.sfb");
        models.add("D.sfb");
        models.add("E.sfb");
        models.add("F.sfb");
        models.add("G.sfb");
        models.add("H.sfb");
        models.add("I.sfb");
        models.add("J.sfb");
        models.add("K.sfb");
        models.add("L.sfb");
        models.add("M.sfb");
        models.add("N.sfb");
        models.add("O.sfb");
        models.add("P.sfb");
        models.add("Q.sfb");
        models.add("R.sfb");
        models.add("S.sfb");
        models.add("T.sfb");
        models.add("U.sfb");
        models.add("V.sfb");
        models.add("W.sfb");
        models.add("X.sfb");
        models.add("Y.sfb");
        models.add("Z.sfb");
    }

    private void createModel(Anchor anchor, String resource) {
        ModelRenderable.builder()
                .setSource(this, Uri.parse(resource))
                .build()
                .thenAccept(modelRenderable -> placeModel(anchor, modelRenderable));
    }

    private void placeModel(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        scene.addChild(anchorNode);
        transformableNode.select();
        transformableNode.setOnTapListener((hitTestResult, motionEvent) -> {
            selectedNode = hitTestResult.getNode();
        });
    }

    private void remove() {
        if (selectedNode != null) {
            AnchorNode an = (AnchorNode) selectedNode.getParent();
            an.removeChild(selectedNode);
            selectedNode = null;
            scene.removeChild(an);
        }
    }

    private void reset() {
        ArrayList<Node> children = new ArrayList<>(arFragment.getArSceneView().getScene().getChildren());
        for (Node node : children) {
            if (node instanceof AnchorNode) {
                if (((AnchorNode) node).getAnchor() != null) {
                    ((AnchorNode) node).getAnchor().detach();
                }
            }
        }
    }

    private void startGame() {
        ObjectAnimator orbitAnimation = createAnimator();
        reset();
        final ArrayList<Vector3> letterPositions = new ArrayList<>();
        gameChangeButton.setVisibility(View.GONE);
        gameRestartButton.setVisibility(View.GONE);
        gameRemoveButton.setVisibility(View.GONE);
        gameStartButton.setVisibility(View.GONE);
        gameDetailsView.setVisibility(View.GONE);
        gameLetterView.setText("0");
        gameCheckButton.setVisibility(View.VISIBLE);
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
        });
        arFragment.getPlaneDiscoveryController().hide();
        modelsRendered = 0;
        for (int k = 0; k < gameSize; k++) {

            String model = getRandomLetterModel();
            generatedLetters.add((model.substring(0, 1)));
            ModelRenderable.builder().setSource(this, Uri.parse(model)).build().thenAccept(modelRenderable -> {

                AnchorNode node = new AnchorNode();
                generatedNodes.add(node);
                nodeStringPairs.put(node, model.substring(0, 1));
                node.setRenderable(modelRenderable);
                scene.addChild(node);

                node.setOnTapListener((hitResult, motionEvent) -> {
                    if (hitResult.getNode() != null) {
                        tappedNode = hitResult.getNode();
                        orbitAnimation.setTarget(tappedNode);
                        orbitAnimation.setDuration(2000);
                        orbitAnimation.start();
                    }
                });

                Random rand = new Random();
                Vector3 vec = getRandomVector(rand);
                if (letterPositions.contains(vec)) {
                    vec = getRandomVector(rand);
                    letterPositions.add(vec);
                } else {
                    letterPositions.add(vec);
                }

                node.setLocalPosition(vec);
                node.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));

                modelsRendered += 1;
                if (modelsRendered >= gameSize) {
                    gameCheckButton.setText("Check Letter");
                    letterSelection("");
                }
            });
        }
        gameCheckButton.setOnClickListener(v -> {
            if (tappedNode == null) {
                speaker.speak("You haven't selected a letter ", 0.5f, 0.5f);
            } else if (nodeStringPairs.get(tappedNode).equals(givenLetter)) {
                score += 1;
                String val = String.valueOf(score);
                gameLetterView.setText(val);
                generatedLetters.remove(nodeStringPairs.get(tappedNode));
                generatedNodes.remove(tappedNode);
                scene.removeChild(tappedNode);
                letterSelection("You Are Correct. next attempt. ");

            } else {
                speaker.speak(" Sorry Wrong Letter. ", 0.5f, 0.5f);
            }
        });
    }

    private void addRandomVector(ArrayList<Vector3> arrayList, Random ran) {

    }

    private Vector3 getRandomVector(Random rand) {
        int x = rand.nextInt(5) - 3;
        int y = rand.nextInt(5) - 3;
        int z = rand.nextInt(5) - 3;
        return new Vector3(new Vector3((float) x, (float) y, (float) z));
    }

    private void letterSelection(String appendix) {
        if (generatedLetters.size() > 0) {
            Random r = new Random();
            int ran = r.nextInt(generatedLetters.size());
            givenLetter = generatedLetters.get(ran);
            speaker.speak(appendix + " please Select letter " + givenLetter, 0.5f, 0.5f);
        } else {
            speaker.speak("You won", 0.5f, 0.5f);
            showEndPopup("Congratulations !!!\nYou won");
        }
    }

    private String getRandomLetterModel() {
        Random random = new Random();
        int ran = random.nextInt(26);
        return models.get(ran);
    }

    private void loadMainActivity() {
        finish();
    }

    private static ObjectAnimator createAnimator() {

        Vector3 v1 = new Vector3(0.4f, 0.4f, 0.4f);
        Vector3 v2 = new Vector3(0.5f, 0.5f, 0.5f);
        Vector3 v3 = new Vector3(0.7f, 0.7f, 0.7f);
        Vector3 v4 = new Vector3(0.5f, 0.5f, 0.5f);
        Vector3 v5 = new Vector3(0.4f, 0.4f, 0.4f);

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

//    private static ObjectAnimator createAnimator() {
//        // Node's setLocalRotation method accepts Quaternions as parameters.
//        // First, set up orientations that will animate a circle.
//        Quaternion orientation1 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 0);
//        Quaternion orientation2 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 120);
//        Quaternion orientation3 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 240);
//        Quaternion orientation4 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 360);
//
//        ObjectAnimator orbitAnimation = new ObjectAnimator();
//        orbitAnimation.setObjectValues(orientation1, orientation2, orientation3, orientation4);
//
//        // Next, give it the localRotation property.
//        orbitAnimation.setPropertyName("localRotation");
//
//        // Use Sceneform's QuaternionEvaluator.
//        orbitAnimation.setEvaluator(new QuaternionEvaluator());
//
//        //  Allow orbitAnimation to repeat forever
//        orbitAnimation.setRepeatCount(ObjectAnimator.INFINITE);
//        orbitAnimation.setRepeatMode(ObjectAnimator.RESTART);
//        orbitAnimation.setInterpolator(new LinearInterpolator());
//        orbitAnimation.setAutoCancel(true);
//
//        return orbitAnimation;
//    }


}

