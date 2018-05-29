package ru.xmn.russiancraftbeer.screens.map.ui

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.swipeDown
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.xmn.russiancraftbeer.R


@RunWith(AndroidJUnit4::class)
@LargeTest
class MapsActivityTest {

    @Rule
    @JvmField
    val mActivityRule = ActivityTestRule<MapsActivity>(
            MapsActivity::class.java)

    @Test
    fun pager_is_visible() {
        val idlingResource = ElapsedTimeIdlingResource(6000)
        IdlingRegistry.getInstance().register(idlingResource)

        onView(allOf(withId(R.id.pubLogo), isDisplayed())).perform(click())
        onView(allOf(withId(R.id.pubDescription), isDisplayed()))
        onView(withId(R.id.viewPager)).perform(swipeDown())
        onView(allOf(withId(R.id.pubDescription), isDescendantOfA(nthChildOf(withId(R.id.viewPager), 0)))).check(matches(not(isDisplayed())))


        onView(allOf(withId(R.id.pubLogo), isDisplayed())).perform(click())
        onView(allOf(withId(R.id.pubDescription), isDisplayed()))
        Espresso.pressBack()
        onView(allOf(withId(R.id.pubDescription), isDescendantOfA(nthChildOf(withId(R.id.viewPager), 0)))).check(matches(not(isDisplayed())))

        Espresso.pressBack()
        onView(allOf(withId(R.id.pubLogo), isDescendantOfA(nthChildOf(withId(R.id.viewPager), 0)))).check(matches(not(isDisplayed())))
    }
}
