//package com.example.chamikanandasiri.interactivebookreader;
//
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import androidx.test.espresso.contrib.RecyclerViewActions;
//import androidx.test.espresso.intent.rule.IntentsTestRule;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.test.rule.ActivityTestRule;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.action.ViewActions.typeText;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.intent.Intents.intended;
//import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
//import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
//import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
//import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
//import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static androidx.test.espresso.matcher.ViewMatchers.withParent;
//import static androidx.test.espresso.matcher.ViewMatchers.withText;
//import static org.hamcrest.Matchers.any;
//import static org.hamcrest.Matchers.not;
//
//@RunWith(AndroidJUnit4.class)
//public class ArActivityTest {
//    @Rule
//    public ActivityTestRule<ArViewActivity> activityActivityTestRule = new ActivityTestRule<>(ArViewActivity.class);
//    @Rule
//    public IntentsTestRule<MenuActivity> MenuActivityActivityTestRule =
//            new IntentsTestRule<>(MenuActivity.class);
//
//    @Test
//    public void ArScreenLoad() {
////        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
////        onView(withId(R.id.remove)).check(matches(not(isDisplayed())));
//        onView(withId(R.id.back)).check(matches(isDisplayed()));
//        onView(withId(R.id.back)).perform(click());
//        intended(hasComponent(MenuActivity.class.getName()));
//    }
//
//    @Test
//    public void arItemClick() {
//        onView(withId(R.id.recyclerview))
//                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
//                        click())).check(matches(hasDescendant(isSelected())));
//    }
//}
