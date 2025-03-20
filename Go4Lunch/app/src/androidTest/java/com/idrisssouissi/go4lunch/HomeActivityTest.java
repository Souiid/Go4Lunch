package com.idrisssouissi.go4lunch;

import android.app.Activity;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import com.idrisssouissi.go4lunch.ui.HomeActivity;
import com.idrisssouissi.go4lunch.ui.RestaurantDetailsActivity;
import com.idrisssouissi.go4lunch.ui.HomeViewModel;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomeActivityTest {

    @Rule
    public ActivityScenarioRule<HomeActivity> activityRule =
            new ActivityScenarioRule<>(HomeActivity.class);

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testNavLunch_WhenRestaurantSelected() {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_lunch));
        Intents.intended(IntentMatchers.hasComponent(RestaurantDetailsActivity.class.getName()));
    }

    @Test
    public void testNavLunch_WhenNoRestaurantSelected() {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_lunch));

        onView(withText(R.string.you_dont_selected_restaurant))
                .inRoot(withDecorView(not(is(getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testNavSettings() {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_settings));

        Intents.intended(IntentMatchers.hasComponent(SettingsActivity.class.getName()));
    }

    @Test
    public void testNavLogout() {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_logout));

        HomeViewModel mockViewModel = mock(HomeViewModel.class);
        verify(mockViewModel).signOut();
    }

    private Activity getActivity() {
        final Activity[] activity = new Activity[1];
        activityRule.getScenario().onActivity(activity1 -> activity[0] = activity1);
        return activity[0];
    }
}

