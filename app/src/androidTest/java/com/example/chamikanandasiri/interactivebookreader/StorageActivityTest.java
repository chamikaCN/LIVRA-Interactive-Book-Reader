package com.example.chamikanandasiri.interactivebookreader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class StorageActivityTest {
    @Rule
    public ActivityTestRule<StorageActivity> activityActivityTestRule = new ActivityTestRule<>(StorageActivity.class);

    @Test
    public void storageScreenLoaded() {
        onView(withId(R.id.StorageEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.StorageSearchButton)).check(matches(isDisplayed()));
        onView(withId(R.id.StorageListView)).check(matches(isDisplayed()));
    }

    @Test
    public void emptyClickButton() {
        onView(withId(R.id.StorageEditText)).perform(typeText(""));
        onView(withId(R.id.StorageSearchButton)).perform(click());
        onView(withId(R.id.StorageListView)).check(matches(hasMinimumChildCount(0)));
    }
}
