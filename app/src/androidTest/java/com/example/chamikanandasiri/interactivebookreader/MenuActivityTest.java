package com.example.chamikanandasiri.interactivebookreader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class MenuActivityTest {
    @Rule
    public ActivityTestRule<MenuActivity> activityActivityTestRule = new ActivityTestRule<>(MenuActivity.class);
    @Rule
    public IntentsTestRule<LibraryActivity> mLoginActivityActivityTestRule =
            new IntentsTestRule<>(LibraryActivity.class);

    @Test
    public void menuScreenLoad() {
        onView(withId(R.id.MenuNameView)).check(matches(isDisplayed()));
        onView(withId(R.id.MenuLogoView)).check(matches(isDisplayed()));
        onView(withId(R.id.MenuGridView)).check(matches(isDisplayed()));
        onView(withId(R.id.MenuRecentBookView)).check(matches(isDisplayed()));
        onView(withId(R.id.MenuRecentBookView)).check(matches(hasChildCount(3)));
    }

    @Test
    public void addBookButtonClick() {
        onView(withId(R.id.AddBookCard)).perform(click());
        onView(withId(R.id.ComplexBarcodeSearchButton)).check(matches(isDisplayed()));
        onView(withId(R.id.ComplexBarcodeSearchButton)).perform(click());
        onView(withId(R.id.BookSearchSearchButton)).check(matches(isDisplayed()));
        onView(withId(R.id.BookSearchEditText)).perform(typeText("sep"));
        onView(withId(R.id.BookSearchSearchButton)).perform(click());
    }

    @Test
    public void SettingsButtonClick() {
        onView(withId(R.id.SettingsCard)).perform(click());
        onView(withId(R.id.SettingsApplyButton)).check(matches(isDisplayed()));
    }

    @Test
    public void LibraryButtonClick() {
        onView(withId(R.id.LibraryCard)).perform(click());
        intended(hasComponent(LibraryActivity.class.getName()));
    }

    @Test
    public void StorageWordButtonClick() {
        onView(withId(R.id.SavedItemsCard)).perform(click());
        onView(withId(R.id.WordLoadButton)).check(matches(isDisplayed()));
        onView(withId(R.id.WordLoadButton)).perform(click());
        intended((hasExtra("type", "word")));
        intended(hasComponent(StorageActivity.class.getName()));
    }

    @Test
    public void StorageCommentButtonClick() {
        onView(withId(R.id.SavedItemsCard)).perform(click());
        onView(withId(R.id.CommentLoadButton)).check(matches(isDisplayed()));
        onView(withId(R.id.CommentLoadButton)).perform(click());
        intended(hasExtraWithKey("type"));
        intended((hasExtra("type", "comment")));
        intended(hasComponent(StorageActivity.class.getName()));
    }

    @Test
    public void GameButtonClick() {
        onView(withId(R.id.AlphaGameCard)).perform(click());
        intended(hasComponent(GameActivity.class.getName()));
    }

    @Test
    public void TextDetectionButtonClick() {
        onView(withId(R.id.TextDetectorCard)).perform(click());
        intended(hasComponent(TextDetectionActivity.class.getName()));
    }

    @Test
    public void RecentBookClick() {
        onView(withId(R.id.MenuRecentBookView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1,
                        click()));
        intended(hasComponent(ArViewActivity.class.getName()));
        intended(hasExtraWithKey("bookID"));
    }


}
