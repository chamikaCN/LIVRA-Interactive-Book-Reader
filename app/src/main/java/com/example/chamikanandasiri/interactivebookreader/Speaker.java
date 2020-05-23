package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class Speaker {
    TextToSpeech speech;
    Context context;

    public Speaker(Context context){
        this.context = context;
        Initialization();
    }

    private void Initialization(){
        speech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = speech.setLanguage(Locale.UK);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "language not supported");
                } else {
                    Log.e("TTS", "initializing failed");
                }
            }
        });
    }

    public void speak(String phrase, float speed, float pitch ) {
        float speedValue = speed;
        if (speedValue < 0.1f) speedValue = 0.1f;
        float pitchValue = pitch;
        if (pitchValue < 0.1f) pitchValue = 0.1f;
        speech.setPitch(pitchValue);
        speech.setSpeechRate(speedValue);
        speech.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void stop(){
        speech.stop();
    }

    public void changePitch(float newValue){
        if (newValue < 0.1) speech.setPitch(newValue);
    }

    public void changeSpeed(float newValue){
        if (newValue < 0.1) speech.setSpeechRate(newValue);
    }
}
