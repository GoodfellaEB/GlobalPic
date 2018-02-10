package com.fufu.epicture

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.fufu.epicture.fragments.HomeFragment
import com.fufu.epicture.imgur.AccessToken
import com.fufu.epicture.imgur.ImgurRequests
import com.fufu.epicture.imgur.RequestHandler
import com.fufu.epicture.listeners.AuthorizationTokenReceivedListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import okhttp3.Response
import java.util.*
import com.fufu.epicture.fragments.AddFragment
import com.fufu.epicture.fragments.LoginFragment


class MainActivity : AppCompatActivity(),
        AuthorizationTokenReceivedListener, RequestHandler {

    enum class Frame {
        LOGIN,
        HOME,
        FAVORITES,
        ADD
    }

    private lateinit var imgurRequests : ImgurRequests
    private lateinit var timer : Timer
    private var currentFrame : Frame = Frame.LOGIN

    companion object {
        private const val LOGIN_TAG : String = "login_frag"
        private const val HOME_TAG : String = "home_frag"
        private const val ADD_TAG : String = "add_frag"
        private const val SHARED_PREF_ACCESS_TOKEN = "accessToken"

        private const val TIME_LAPSE : Long = 5 * 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imgurRequests = ImgurRequests(this)
        setContentView(R.layout.core_layout)
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
                goToHomeFragment(accessToken)
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

        swapFragment(R.id.core_container, LoginFragment(), LOGIN_TAG)
    }

    /*
        Implementation AuthorizationTokenReceivedListener Interface
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
            goToHomeFragment(accessToken)
        }
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
        var fragment : android.app.Fragment? = fragmentManager.findFragmentByTag(LOGIN_TAG)

        if (fragment != null)
            fragmentManager.beginTransaction().remove(fragment).commit()
        fragment = fragmentManager.findFragmentByTag(HOME_TAG)
        if (fragment != null)
            fragmentManager.beginTransaction().remove(fragment).commit()
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

    private fun reloadFrames() {
        val accessToken = extractAccessToken()

        if (accessToken != null) {
            when (currentFrame) {
                Frame.LOGIN -> goToHomeFragment(accessToken)
                Frame.FAVORITES -> goToHomeFragment(accessToken)
                Frame.ADD -> goToHomeFragment(accessToken)
            }
        }
    }

    fun goToHome(menuItem: MenuItem) {
        Log.d("DEBUG", "goToHome")
        val accessToken = extractAccessToken()

        if (accessToken != null)
            goToHomeFragment(accessToken)
    }

    fun goToFavorites(menuItem: MenuItem) {
        Log.d("DEBUG", "goToFavorites")
        val accessToken = extractAccessToken()

        if (accessToken != null)
            goToFavoritesFragment(accessToken)
    }

    fun goToAdd(menuItem: MenuItem) {
        Log.d("DEBUG", "goToAdd")
        val accessToken = extractAccessToken()

        if (accessToken != null)
            goToAddFragment(accessToken)
    }

    private fun goToHomeFragment(accessToken: AccessToken) {
        currentFrame = Frame.HOME
        setBottomNavigationViewVisibility(View.VISIBLE)
        swapFragment(R.id.core_container, getHomeFragment(accessToken), HOME_TAG)
    }

    private fun goToFavoritesFragment(accessToken: AccessToken) {
        currentFrame = Frame.ADD
        setBottomNavigationViewVisibility(View.VISIBLE)
        swapFragment(R.id.core_container, getAddFragment(accessToken), ADD_TAG)
    }

    private fun goToAddFragment(accessToken: AccessToken) {
        currentFrame = Frame.ADD
        setBottomNavigationViewVisibility(View.VISIBLE)
        swapFragment(R.id.core_container, getAddFragment(accessToken), ADD_TAG)
    }

    private fun getHomeFragment(accessToken: AccessToken) : HomeFragment {
        val accessTokenJson : String = Gson().toJson(accessToken)
        val homeFragment  = HomeFragment()
        val bundle = Bundle()

        bundle.putString("accessToken", accessTokenJson)
        homeFragment.arguments = bundle
        return (homeFragment)
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
            reloadFrames()
        } else {
            Log.d("DEBUG", "json format error -> object expected")
        }
    }

    override fun onAccountImagesResponse(response: Response) {

    }

    override fun onImageUploadResponse(response: Response) {

    }
}
