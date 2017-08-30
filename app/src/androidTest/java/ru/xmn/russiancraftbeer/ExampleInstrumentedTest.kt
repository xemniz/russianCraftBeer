package ru.xmn.russiancraftbeer

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.IdlingPolicies
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.text.format.DateUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.util.concurrent.TimeUnit
import android.support.test.espresso.Espresso
import ElapsedTimeIdlingResource
import android.support.test.espresso.IdlingResource
import ru.xmn.russiancraftbeer.screens.map.ui.MapsActivity


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("ru.xmn.russiancraftbeer", appContext.packageName)
    }

    @get:Rule
    public var mActivityRule = ActivityTestRule<MapsActivity>(
            MapsActivity::class.java)


    @Test
    fun helpOpen(){

        var waitingTime = DateUtils.SECOND_IN_MILLIS * 5

        IdlingPolicies.setMasterPolicyTimeout(
                waitingTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(
                waitingTime * 2, TimeUnit.MILLISECONDS);


        val idlingResource = ElapsedTimeIdlingResource(waitingTime)
        Espresso.registerIdlingResources(idlingResource)

        onView(withId(R.id.help_button)).perform(click())

        Espresso.registerIdlingResources(idlingResource)

        onView(withId(R.id.help_card)).check(matches(isDisplayed()))

        Espresso.unregisterIdlingResources(idlingResource);
    }
}
