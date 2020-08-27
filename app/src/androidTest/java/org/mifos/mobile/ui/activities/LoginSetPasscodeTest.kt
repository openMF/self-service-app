package org.mifos.mobile.ui.activities

import android.view.View
import android.view.ViewGroup

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mifos.mobile.R
import org.mifos.mobile.utils.EspressoIdlingResouce

/**
 * UI Testing for Login using Espresso
 *
 * @author Ashwin Ramakrishnan
 * @since 22/08/2020
 */

@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginSetPasscodeTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(SplashActivity::class.java)

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.READ_PHONE_STATE)

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResouce.countingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResouce.countingIdlingResource)
    }

    @Test
    fun loginSetPasscodeTest() {
        val usernameEditText = onView(
            allOf(
                childAtPosition(
                    childAtPosition(
                        withId(R.id.til_username),
                        0
                    ),
                    0
                )
            )
        )

        val passwordEditText = onView(
            allOf(
                childAtPosition(
                    childAtPosition(
                        withId(R.id.til_password),
                        0
                    ),
                    0
                )
            )
        )

        val showPasswordToggle = onView(
            allOf(
                withId(R.id.text_input_password_toggle), withContentDescription("Show password"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.til_password),
                        0
                    ),
                    1
                )
            )
        )

        val loginButton = onView(
            allOf(
                withId(R.id.btn_login), withText("Login"),
                childAtPosition(
                    allOf(
                        withId(R.id.ll_login),
                        childAtPosition(
                            withClassName(`is`("androidx.core.widget.NestedScrollView")),
                            0
                        )
                    ),
                    3
                )
            )
        )

        val zeroButtonPasscode = onView(
            allOf(
                withId(R.id.btn_zero), withText("0"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.TableLayout")),
                        3
                    ),
                    1
                )
            )
        )

        val passcodeLayout: ViewInteraction = onView(withId(R.id.cl_rootview))

        val passcodeVisibilityButton = onView(
            allOf(
                withId(R.id.iv_visibility),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.LinearLayout")),
                        3
                    ),
                    1
                )
            )
        )

        val proceedButton = onView(allOf(withId(R.id.btn_save), withText("Proceed")))

        val appCompatButton12 = onView(allOf(withId(R.id.btn_save), withText("Save")))

        //Test actions

        usernameEditText.perform(click(), replaceText("ashwinr"), closeSoftKeyboard())

        passwordEditText.perform(click(), replaceText("password"), closeSoftKeyboard())

        for (i in 1..2) showPasswordToggle.perform(click())

        loginButton.perform(click())

        passcodeLayout.check(matches(isDisplayed()))

        passcodeVisibilityButton.perform(click())

        passcodeLayout.perform(swipeUp()) //To make the lower part of the screen visible in smaller devices

        for (i in 1..4) zeroButtonPasscode.perform(click()) // Inputs passcode as 0000

        proceedButton.perform(click())

        passcodeLayout.perform(swipeUp()) //To make the lower part of the screen visible in smaller devices

        for (i in 1..4) zeroButtonPasscode.perform(click()) // Inputs passcode as 0000

        appCompatButton12.perform(click())
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}