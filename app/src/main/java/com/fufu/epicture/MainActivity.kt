package com.fufu.epicture

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.fufu.epicture.fragments.HomeFragment
import com.fufu.epicture.imgur.AccessToken
import com.fufu.epicture.imgur.ImgurRequests
import com.fufu.epicture.imgur.RequestHandler
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import okhttp3.Response
import java.util.*
import com.fufu.epicture.fragments.AddFragment
import com.fufu.epicture.fragments.FragmentType
import com.fufu.epicture.fragments.LoginFragment
import com.fufu.epicture.listeners.FragmentsListener


class MainActivity : AppCompatActivity(), FragmentsListener, RequestHandler {

    enum class Frame(val pos: Int) {
        LOGIN(0),
        HOME(1),
        FAVORITES(2),
        ADD(3)
    }

    private lateinit var imgurRequests : ImgurRequests
    private lateinit var timer : Timer
    private val stateReloadFrames : BooleanArray = kotlin.BooleanArray(Frame.values().size)
    private var currentFrame : Frame = Frame.LOGIN

    enum class FragmentsTAG(val tag: String) {
        LOGIN_TAG("login_frag"),
        HOME_TAG("home_frag"),
        FAVORITES_TAG("favorites_frag"),
        ADD_TAG("add_frag")
    }

    companion object {
        private const val SHARED_PREF_ACCESS_TOKEN = "accessToken"

        private const val TIME_LAPSE : Long = 5 * 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.core_layout)

        imgurRequests = ImgurRequests(this)
        setBottomNavigationViewVisibility(View.GONE)
        tryImgurConnection()
        timer = Timer()
        timer.scheduleAtFixedRate(TokenRefresher(this), 0, TIME_LAPSE)
    }

    private fun tryImgurConnection() {
        val accessToken : AccessToken? = extractAccessToken()

        if (accessToken != null) {
            if (accessToken.isExpired())
                imgurRequests.refreshToken(accessToken)
            else
                goToHomeFragment(accessToken, true)
        }
    }

    private fun setBottomNavigationViewVisibility(state: Int) {
        val view : View = findViewById(R.id.bottom_navigation_view)

        view.visibility = state
    }

    /*
        onClick Login Button
     */

    fun onTryLogin(view: View) {
        Log.d("DEBUG", "onTryLogin")

        swapFragment(R.id.core_container, LoginFragment(), FragmentsTAG.LOGIN_TAG.tag)
    }

    /*
        Implementation FragmentsListener Interface
     */

    override fun onAuthorizationTokenReceived(accessToken: AccessToken) {
        Log.d("DEBUG", "in MainActivity AuthorizationTokenReceived")
        if (accessToken.isExpired()) {
            // set an error message Snackbar
            Log.d("DEBUG", "token expiré")
            logout()
        } else {
            Log.d("DEBUG", "connexion effectuée")
            emptyBackStack()
            storeAccessToken(accessToken)
            goToHomeFragment(accessToken, true)
        }
    }

    override fun onImageUploaded() {
        if (currentFrame != Frame.HOME)
            stateReloadFrames[Frame.HOME.pos] = true
        if (currentFrame != Frame.FAVORITES)
            stateReloadFrames[Frame.FAVORITES.pos] = true
        if (currentFrame != Frame.ADD)
            reloadFrame()
    }

    private fun swapFragment(containerViewId: Int, fragment: Fragment, tag: String? = null) {
        val transaction : FragmentTransaction = supportFragmentManager.beginTransaction()

        transaction.replace(containerViewId, fragment, tag)
        transaction.addToBackStack(tag)
        transaction.commit()
    }

    private fun logout() {
        removeAccessToken()
        emptyBackStack()
        removeAllFragment()
        setBottomNavigationViewVisibility(View.GONE)
    }

    private fun removeAllFragment() {
        var fragment : Fragment? = supportFragmentManager.findFragmentByTag(FragmentsTAG.LOGIN_TAG.tag)

        if (fragment != null)
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        fragment = supportFragmentManager.findFragmentByTag(FragmentsTAG.HOME_TAG.tag)
        if (fragment != null)
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        fragment = supportFragmentManager.findFragmentByTag(FragmentsTAG.FAVORITES_TAG.tag)
        if (fragment != null)
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        fragment = supportFragmentManager.findFragmentByTag(FragmentsTAG.ADD_TAG.tag)
        if (fragment != null)
            supportFragmentManager.beginTransaction().remove(fragment).commit()
    }

    private fun emptyBackStack() {
        while (supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStackImmediate()
    }

    private fun storeAccessToken(accessToken: AccessToken) {
        val accessTokenJson : String = Gson().toJson(accessToken)
        val editor : SharedPreferences.Editor
                = PreferenceManager.getDefaultSharedPreferences(this).edit()

        editor.putString(SHARED_PREF_ACCESS_TOKEN, accessTokenJson)
        editor.apply()
    }

    private fun extractAccessToken() : AccessToken? {
        val accessTokenJson : String? = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SHARED_PREF_ACCESS_TOKEN, null)

        if (TextUtils.isEmpty(accessTokenJson))
            return (null)
        return try {
            Gson().fromJson<AccessToken>(accessTokenJson, AccessToken::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    private fun removeAccessToken() {
        val editor : SharedPreferences.Editor
                = PreferenceManager.getDefaultSharedPreferences(this).edit()

        editor.remove(SHARED_PREF_ACCESS_TOKEN)
        editor.apply()
    }

    private fun reloadFrame() {
        val accessToken = extractAccessToken()

        if (accessToken != null) {
            when (currentFrame) {
                Frame.LOGIN -> goToHomeFragment(accessToken, true)
                Frame.HOME -> goToHomeFragment(accessToken, true)
                Frame.FAVORITES -> goToFavoritesFragment(accessToken, true)
                Frame.ADD -> goToAddFragment(accessToken, true)
            }
        }
    }

    fun goToHome(menuItem: MenuItem) {
        Log.d("DEBUG", "goToHome")
        val accessToken = extractAccessToken()

        if (accessToken != null) {
            goToHomeFragment(accessToken, stateReloadFrames[Frame.HOME.pos])
            stateReloadFrames[Frame.HOME.pos] = false
        }
    }

    fun goToFavorites(menuItem: MenuItem) {
        Log.d("DEBUG", "goToFavorites")
        val accessToken = extractAccessToken()

        if (accessToken != null) {
            goToFavoritesFragment(accessToken, stateReloadFrames[Frame.FAVORITES.pos])
            stateReloadFrames[Frame.FAVORITES.pos] = false
        }
    }

    fun goToAdd(menuItem: MenuItem) {
        Log.d("DEBUG", "goToAdd")
        val accessToken = extractAccessToken()

        if (accessToken != null) {
            goToAddFragment(accessToken, stateReloadFrames[Frame.ADD.pos])
            stateReloadFrames[Frame.ADD.pos] = false
        }
    }

    private fun goToHomeFragment(accessToken: AccessToken, new: Boolean) {
        val fragment : Fragment? = supportFragmentManager.findFragmentByTag(FragmentsTAG.HOME_TAG.tag)

        currentFrame = Frame.HOME
        setBottomNavigationViewVisibility(View.VISIBLE)
        if (fragment == null || new)
            swapFragment(R.id.core_container, getHomeFragment(accessToken), FragmentsTAG.HOME_TAG.tag)
        else
            swapFragment(R.id.core_container, fragment, FragmentsTAG.HOME_TAG.tag)
    }

    /*
    * Favorites Fragment is the HomeFragment with Type Favorite
    * */
    private fun goToFavoritesFragment(accessToken: AccessToken, new: Boolean) {
        val fragment : Fragment? = supportFragmentManager.findFragmentByTag(FragmentsTAG.FAVORITES_TAG.tag)

        currentFrame = Frame.ADD
        setBottomNavigationViewVisibility(View.VISIBLE)
        if (fragment == null || new)
            swapFragment(R.id.core_container, getFavoritesFragment(accessToken), FragmentsTAG.FAVORITES_TAG.tag)
        else
            swapFragment(R.id.core_container, fragment, FragmentsTAG.FAVORITES_TAG.tag)
    }

    private fun goToAddFragment(accessToken: AccessToken, new: Boolean) {
        val fragment : Fragment? = supportFragmentManager.findFragmentByTag(FragmentsTAG.ADD_TAG.tag)

        currentFrame = Frame.ADD
        setBottomNavigationViewVisibility(View.VISIBLE)
        if (fragment == null || new)
            swapFragment(R.id.core_container, getAddFragment(accessToken), FragmentsTAG.ADD_TAG.tag)
        else
            swapFragment(R.id.core_container, fragment, FragmentsTAG.ADD_TAG.tag)
    }

    private fun getHomeFragment(accessToken: AccessToken) : HomeFragment {
        val accessTokenJson : String = Gson().toJson(accessToken)
        val homeFragment  = HomeFragment()
        val bundle = Bundle()

        bundle.putString("accessToken", accessTokenJson)
        bundle.putString("fragmentType", Gson().toJson(FragmentType.NORMAL))
        homeFragment.arguments = bundle
        return (homeFragment)
    }

    private fun getFavoritesFragment(accessToken: AccessToken) : HomeFragment {
        val accessTokenJson : String = Gson().toJson(accessToken)
        val favoritesFragment  = HomeFragment()
        val bundle = Bundle()

        bundle.putString("accessToken", accessTokenJson)
        bundle.putString("fragmentType", Gson().toJson(FragmentType.FAVORITE))
        favoritesFragment.arguments = bundle
        return (favoritesFragment)
    }

    private fun getAddFragment(accessToken: AccessToken) : AddFragment {
        val accessTokenJson : String = Gson().toJson(accessToken)
        val addFragment  = AddFragment()
        val bundle = Bundle()

        bundle.putString("accessToken", accessTokenJson)
        addFragment.arguments = bundle
        return (addFragment)
    }

    private class TokenRefresher(mainActivity: MainActivity) : TimerTask() {

        private val master = mainActivity

        override fun run() {
            master.runOnUiThread({ master.refreshTokenIfNeeded() })
        }
    }

    fun refreshTokenIfNeeded() {
        val accessToken : AccessToken? = extractAccessToken()

        if (accessToken != null && accessToken.isExpired())
            imgurRequests.refreshToken(accessToken)
    }

    /*
        Implementation RequestHandler Interface
     */

    override fun onRefreshTokenResponse(response: Response) {
        Log.d("DEBUG", "onRefreshTokenResponse")

        val jsonTree = JsonParser().parse(response.body()?.string())
        val jsonObject : JsonObject
        val accessToken = AccessToken("")

        if (jsonTree.isJsonObject) {
            jsonObject = jsonTree.asJsonObject
            accessToken.parseJsonObject(jsonObject)
            storeAccessToken(accessToken)
            reloadFrame()
        } else {
            Log.d("DEBUG", "json format error -> object expected")
        }
    }

    override fun onAccountImagesResponse(response: Response) {

    }

    override fun onImageUploadResponse(response: Response) {

    }

    override fun onImageUploadFail() {

    }
}
