package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class ToastManager {
    private Context context;
    private float speed, pitch;
    private Speaker speaker;
    private boolean voiceSupportActive, isVoiceSupportSettingsConfigurable;

    public ToastManager(Context cont) {
        context = cont;
        speaker = new Speaker(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        speed = sharedPreferences.getFloat("Speed", 0.5f);
        pitch = sharedPreferences.getFloat("Pitch", 0.5f);
        voiceSupportActive = sharedPreferences.getBoolean("VoiceSupport",false);
        isVoiceSupportSettingsConfigurable = sharedPreferences.getBoolean("VoiceConfig",false);
    }

    public void showLongToast(String phrase) {
        Toast.makeText(context, phrase, Toast.LENGTH_LONG).show();
        if (voiceSupportActive) {
            speakToast(phrase);
        }
    }

    public void showShortToast(String phrase) {
        Toast.makeText(context, phrase, Toast.LENGTH_SHORT).show();
        if (voiceSupportActive) {
            speakToast(phrase);
        }
    }

    private void speakToast(String phrase) {
        if (isVoiceSupportSettingsConfigurable) {
            speaker.speak(phrase, speed, pitch);
        }else{
            speaker.speak(phrase, 0.5f, 0.5f);
        }
    }

    public void setVoiceSupport(boolean value) {
        voiceSupportActive = value;
    }

    public boolean getVoiceSupport(){
        return voiceSupportActive;
    }

    public void setVoiceSupportConfigurability(boolean value) {
        isVoiceSupportSettingsConfigurable = value;
    }

    public boolean getVoiceSupportConfigurability(){
        return isVoiceSupportSettingsConfigurable;
    }
}
