package ru.xmn.russiancraftbeer.screens.map.ui

import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.agoda.kakao.KView
import com.agoda.kakao.KViewPager
import com.agoda.kakao.Screen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.xmn.russiancraftbeer.R


@RunWith(AndroidJUnit4::class)
@LargeTest
class MapsActivityTest3 {

    @Rule
    @JvmField
    val mActivityRule = ActivityTestRule<MapsActivity>(
            MapsActivity::class.java)

    @Test
    fun pager_is_visible() {
        val idlingResource = ElapsedTimeIdlingResource(6000)
        IdlingRegistry.getInstance().register(idlingResource)

        val screen = MapScreen()
        screen {
            pubLogo {
                isDisplayed()
                click()
            }
            pubDescription.isDisplayed()
            viewPager {
                isDisplayed()
                swipeDown()
            }
            pubDescription.isNotDisplayed()

            pubLogo.click()
            pressBack()
            pubDescription.isNotDisplayed()
            pressBack()
            pubLogo.isNotDisplayed()
        }
    }
}

class MapScreen : Screen<MapScreen>() {
    val pubLogo = KView { withMatcher(R.id.pubLogo.descendantOfMatcher(R.id.viewPager)) }
    val pubDescription = KView { withMatcher(R.id.pubDescription.descendantOfMatcher(R.id.viewPager)) }
    val viewPager = KViewPager { withId(R.id.viewPager) }
}