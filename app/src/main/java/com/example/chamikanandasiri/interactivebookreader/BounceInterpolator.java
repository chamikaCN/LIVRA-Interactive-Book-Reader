package com.example.chamikanandasiri.interactivebookreader;

import android.view.animation.Interpolator;

public class BounceInterpolator implements Interpolator {

    private double amplitude = 1;
    private double frequency = 10;

    private String TAG ="Test";

    BounceInterpolator(double amplitude, double frequency) {
        this.amplitude = amplitude;
        this.frequency = frequency;
    }
    @Override
    public float getInterpolation(float time) {
        return (float) (-1 * Math.pow(Math.E, -time/ amplitude) *
                Math.cos(frequency * time) + 1);
    }
}
