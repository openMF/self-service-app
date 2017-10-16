package org.mifos.selfserviceapp.tests;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.selfserviceapp.MifosSelfServiceApp;
import org.mifos.selfserviceapp.R;
import org.mifos.selfserviceapp.api.MockedBaseApiManager;
import org.mifos.selfserviceapp.models.client.Client;
import org.mifos.selfserviceapp.ui.activities.HomeActivity;
import org.mifos.selfserviceapp.utils.Constants;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by dilpreet on 22/7/17.
 */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class HomeActivityTest {

    MockedBaseApiManager mockedBaseApiManager;
    long clientId = 1;

    @Rule
    public ActivityTestRule<HomeActivity> activityTestRule = new
            ActivityTestRule<HomeActivity>(HomeActivity.class) {
            @Override
            protected Intent getActivityIntent() {
                Context targetContext = InstrumentationRegistry.getInstrumentation()
                        .getTargetContext();
                Intent result = new Intent(targetContext, HomeActivity.class);
                result.putExtra(Constants.CLIENT_ID, clientId);
                return result;
            }
        };

    @Before
    public void setUp() {
        mockedBaseApiManager = (MockedBaseApiManager) ((MifosSelfServiceApp) activityTestRule.
                getActivity().getApplicationContext()).component().baseApiManager();
    }

    @Test
    public void someTest() {
        Client client = mockedBaseApiManager.getClientsApi().getClientForId(clientId).toBlocking().
                first();
        onView(allOf(ViewMatchers.withId(R.id.tv_user_name), withText(client.getDisplayName())))
                .check(matches(withText(client.getDisplayName())));
    }
}
