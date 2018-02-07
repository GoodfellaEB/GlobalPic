package com.fufu.epicture

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.util.Log
import android.view.View
import com.google.gson.Gson


class MainActivity : AppCompatActivity(), AuthorizationTokenReceivedListener {

    companion object {
        private const val LOGIN_TAG : String = "login_frag"
        private const val SHARED_PREF_ACCESS_TOKEN = "accessToken"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }

    fun onTryLogin(view: View) {
        Log.d("DEBUG", "onTryLogin")

        val loginFragment = LoginFragment()
        val transaction : FragmentTransaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.main_page_fragment, loginFragment, LOGIN_TAG)
        transaction.addToBackStack(LOGIN_TAG)
        transaction.commit()
    }

    override fun onAuthorizationTokenReceived(accessToken: AccessToken) {
        if (accessToken.isExpired()) {
            // set an error message Snackbar
            logout()
        } else {
            emptyBackStack()
            storeAccessToken(accessToken)
            // change fragment
            Log.d("DEBUG", "connexion effectuée")
        }
    }

    private fun logout() {
        removeAccessToken()
        emptyBackStack()
        // change fragment
    }

    private fun emptyBackStack() {
        while (supportFragmentManager.backStackEntryCount > 0)
            fragmentManager.popBackStackImmediate()
    }

    private fun storeAccessToken(accessToken: AccessToken) {
        val accessTokenJson : String = Gson().toJson(accessToken)
        val editor : SharedPreferences.Editor
                = PreferenceManager.getDefaultSharedPreferences(this).edit()

        editor.putString(SHARED_PREF_ACCESS_TOKEN, accessTokenJson)
        editor.apply()
    }

    private fun removeAccessToken() {
        val editor : SharedPreferences.Editor
                = PreferenceManager.getDefaultSharedPreferences(this).edit()

        editor.remove(SHARED_PREF_ACCESS_TOKEN)
        editor.apply()
    }
}
