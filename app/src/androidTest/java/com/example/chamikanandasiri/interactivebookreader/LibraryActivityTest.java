package com.example.chamikanandasiri.interactivebookreader;

import android.app.Activity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class LibraryActivityTest {
    private LibraryActivity libraryActivity;
    @Rule
    public ActivityTestRule<LibraryActivity> activityActivityTestRule=new ActivityTestRule<>(LibraryActivity.class);
    @Before
    public void setUp() throws Exception {
        libraryActivity=activityActivityTestRule.getActivity();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void onCreate() {

    }
}