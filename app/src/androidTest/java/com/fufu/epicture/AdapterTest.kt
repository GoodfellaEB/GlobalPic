package com.fufu.epicture

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.fufu.epicture.display.EpictureImage
import com.fufu.epicture.display.ImageAdapter
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import android.support.test.rule.ActivityTestRule
import android.support.test.rule.UiThreadTestRule
import org.junit.Rule


/**
 * Created by weryp on 2/11/18.
 */

@RunWith(AndroidJUnit4::class)
class AdapterTest {

    @get:Rule
    var activityRule = ActivityTestRule<MainActivity>(MainActivity::class.java)


    @Test
    fun adapterAddElementTest() {
        val activity : MainActivity = activityRule.activity
        activity.runOnUiThread({
            val adapter = ImageAdapter(activity, R.layout.grid_view_item)
            val epictureImage : EpictureImage = EpictureImage("chien", "", null , null)

            Assert.assertEquals(0, adapter.count)
            adapter.add(epictureImage)
            Assert.assertEquals(1, adapter.count)
        })
    }

    @Test
    fun adapterFavoritesFilterTest() {
        val activity : MainActivity = activityRule.activity
        activity.runOnUiThread({
            val adapter = ImageAdapter(activity, R.layout.grid_view_item)
            val one : EpictureImage = EpictureImage("chien", "", null , null)
            val two : EpictureImage = EpictureImage("chat", "", null , null)
            val three : EpictureImage = EpictureImage("lapin", "", null , null)
            val four : EpictureImage = EpictureImage("souris", "", null , null)
            val favorites : ArrayList<String> = ArrayList()

            adapter.add(one)
            adapter.add(two)
            adapter.add(three)
            adapter.add(four)
            favorites.add(two.getId())
            favorites.add(three.getId())
            Assert.assertEquals(4, adapter.count)
            adapter.setListViewsToDisplay(favorites)
            adapter.filter.filter("", {
                Assert.assertEquals(2, adapter.count)
            })
        })

    }

    @Test
    fun adapterOccurenceFilterTest() {
        val activity : MainActivity = activityRule.activity
        activity.runOnUiThread({
            val adapter = ImageAdapter(activity, R.layout.grid_view_item)
            val one : EpictureImage = EpictureImage("chien", "", null , null)
            val two : EpictureImage = EpictureImage("chat", "", null , null)
            val three : EpictureImage = EpictureImage("lapin", "", null , null)
            val four : EpictureImage = EpictureImage("souris", "", null , null)

            adapter.add(one)
            adapter.add(two)
            adapter.add(three)
            adapter.add(four)
            Assert.assertEquals(4, adapter.count)
            adapter.setFilter("ch")
            adapter.filter.filter("", {
                Assert.assertEquals(2, adapter.count)
                adapter.setFilter("chi")
                adapter.filter.filter("", {
                    Assert.assertEquals(1, adapter.count)
                })
            })
        })
    }

    @Test
    fun adapterTitleFilterTest() {
        val activity : MainActivity = activityRule.activity
        activity.runOnUiThread({
            val adapter = ImageAdapter(activity, R.layout.grid_view_item)
            val one : EpictureImage = EpictureImage("chien", "", null , null)
            val two : EpictureImage = EpictureImage("chat", "", null , null)
            val three : EpictureImage = EpictureImage("lapin", "", null , null)
            val four : EpictureImage = EpictureImage("souris", "", null , null)

            adapter.add(one)
            adapter.add(two)
            adapter.add(three)
            adapter.add(four)
            Assert.assertEquals(4, adapter.count)
            adapter.setFilterType(ImageAdapter.FilterType.WITH_TITLE)
            adapter.filter.filter("", {
                Assert.assertEquals(0, adapter.count)

                val five : EpictureImage = EpictureImage("cheval", "", "Petit cheval" , null)
                val six : EpictureImage = EpictureImage("mouton", "", "Grand mouton" , null)

                adapter.add(five)
                adapter.add(six)
                adapter.filter.filter("", {
                    Assert.assertEquals(2, adapter.count)
                })
            })
        })
    }

    @Test
    fun adapterDescriptionFilterTest() {
        val activity : MainActivity = activityRule.activity
        activity.runOnUiThread({
            val adapter = ImageAdapter(activity, R.layout.grid_view_item)
            val one : EpictureImage = EpictureImage("chien", "", null , null)
            val two : EpictureImage = EpictureImage("chat", "", null , null)
            val three : EpictureImage = EpictureImage("lapin", "", null , null)
            val four : EpictureImage = EpictureImage("souris", "", null , null)

            adapter.add(one)
            adapter.add(two)
            adapter.add(three)
            adapter.add(four)
            Assert.assertEquals(4, adapter.count)
            adapter.setFilterType(ImageAdapter.FilterType.WITH_DESCRIPTION)
            adapter.filter.filter("", {
                Assert.assertEquals(0, adapter.count)

                val five : EpictureImage = EpictureImage("cheval", "", null ,
                        "Le cheval marche dans la plaine")
                val six : EpictureImage = EpictureImage("mouton", "", null ,
                        "Le mouton à l'air bien chaud et doux")
                val seven : EpictureImage = EpictureImage("lama", "", null ,
                        "Attention il crache !")

                adapter.add(five)
                adapter.add(six)
                adapter.add(seven)
                adapter.filter.filter("", {
                    Assert.assertEquals(3, adapter.count)
                })
            })
        })
    }
}