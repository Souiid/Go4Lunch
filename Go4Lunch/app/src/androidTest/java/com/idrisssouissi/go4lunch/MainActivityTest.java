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

// Utilisé si vous voulez matcher HomeActivity
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
// ...
// D'autres imports selon vos besoins

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
        // 1) On "stub" l'Intent externe : TOUT ce qui n'est pas "interne" à l'app

        intending(anyIntent())
                .respondWith(new Instrumentation.ActivityResult(
                        Activity.RESULT_OK,
                        new Intent()
                ));

        // 2) On vérifie que le bouton de connexion est affiché, puis on clique dessus
        onView(withId(R.id.google_sign_in_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // 3) Puisque vous gérez la réception du résultat dans onActivityResult(...),
        //    votre code devrait lancer HomeActivity. On vérifie donc :
        intended(hasComponent(HomeActivity.class.getName()));
    }
}