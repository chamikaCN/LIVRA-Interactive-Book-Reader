package com.example.chamikanandasiri.interactivebookreader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;

@RunWith(AndroidJUnit4.class)
public class LibraryActivityTest {
    @Rule
    public ActivityTestRule<LibraryActivity> activityActivityTestRule = new ActivityTestRule<>(LibraryActivity.class);

    @Test
    public void LibraryScreenLoad() {
        onView(withId(R.id.LibraryBookDetailsButton)).check(matches(isDisplayed()));
        onView(withId(R.id.LibrarySearchButton)).check(matches(isDisplayed()));
        onView(withId(R.id.LibraryEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.LibraryGridView)).check(matches(isDisplayed()));
        onView(withId(R.id.LibraryGridView)).check(matches(hasMinimumChildCount(1)));
    }

    @Test
    public void LibraryDeletePopupLoad() {
        onView(withId(R.id.LibraryGridView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1,
                        longClick())).check(matches(hasDescendant(isSelected())));
        onView(withId(R.id.LibraryBookDetailsButton)).perform(click());
        onView(withId(R.id.DeleteOKButton)).check(matches(isDisplayed()));
        onView(withId(R.id.DeleteListView)).check(matches(hasMinimumChildCount(1)));
    }

    @Test
    public void LibrarySearch() {
        onView(withId(R.id.LibraryEditText)).perform(typeText("sep"));
        onView(withId(R.id.LibrarySearchButton)).perform(click());
        onView(withId(R.id.LibraryGridView)).check(matches(hasChildCount(1)));
    }
}