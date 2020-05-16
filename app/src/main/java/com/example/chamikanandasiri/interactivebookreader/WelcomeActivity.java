package com.example.chamikanandasiri.interactivebookreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class WelcomeActivity extends AppCompatActivity {

    private String currentTheme;
    private ImageView welcomeNameView,welcomeLogoView;

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
        setContentView(R.layout.activity_welcome);

        welcomeLogoView = findViewById(R.id.WelcomeLogoImageView);
        welcomeNameView = findViewById(R.id.WelcomeNameImageView);

        Animation logo_anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.logo_welcome);
        welcomeLogoView.setAnimation(logo_anim);

        Animation name_anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.name_welcome);
        welcomeNameView.setAnimation(name_anim);

        logo_anim.setAnimationListener(new Animation.AnimationListener(){

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                finish();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        name_anim.setAnimationListener(new Animation.AnimationListener(){

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                finish();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
