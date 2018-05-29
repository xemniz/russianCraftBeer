package ru.xmn.russiancraftbeer.screens.map.ui

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.zhuinden.espressohelper.*
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.R.id.*


@RunWith(AndroidJUnit4::class)
@LargeTest
class MapsActivityTest2 {

    @Rule
    @JvmField
    val mActivityRule = ActivityTestRule<MapsActivity>(
            MapsActivity::class.java)

    @Test
    fun pager_is_visible() {
        val idlingResource = ElapsedTimeIdlingResource(6000)
        IdlingRegistry.getInstance().register(idlingResource)

        pubLogo.descendantOf(viewPager).performClick()
        pubLogo.descendantOf(viewPager).checkIsDisplayed()
        viewPager.performSwipeDown()
        pubDescription.descendantOf(viewPager).checkIsNotDisplayed()


        pubLogo.descendantOf(viewPager).performClick()
        Espresso.pressBack()
        pubDescription.descendantOf(viewPager).checkIsNotDisplayed()

        Espresso.pressBack()
        pubLogo.descendantOf(viewPager).checkIsNotDisplayed()
    }

}

fun Int.descendantOf(parentId: Int, position: Int = 0) =
        onView(descendantOfMatcher(parentId, position))!!

fun Int.descendantOfMatcher(parentId: Int, position: Int = 0) =
        allOf(withId(this), isDescendantOfA(nthChildOf(withId(parentId), position)))!!
