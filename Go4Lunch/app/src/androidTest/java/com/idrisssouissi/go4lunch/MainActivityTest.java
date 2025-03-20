package com.idrisssouissi.go4lunch;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.intent.Intents;
import com.idrisssouissi.go4lunch.ui.HomeActivity;
import com.idrisssouissi.go4lunch.ui.MainActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testGoogleSignInButton_opensHomeActivity_afterSuccessfulSignIn() {
        intending(anyIntent())
                .respondWith(new Instrumentation.ActivityResult(
                        Activity.RESULT_OK,
                        new Intent()
                ));

        onView(withId(R.id.google_sign_in_button))
                .check(matches(isDisplayed()))
                .perform(click());

        intended(hasComponent(HomeActivity.class.getName()));
    }
}