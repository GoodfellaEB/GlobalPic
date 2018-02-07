package com.fufu.epicture

/**
 * Created by weryp on 2/7/18.
 */

interface AuthorizationTokenReceivedListener {

    fun onAuthorizationTokenReceived(accessToken: AccessToken)
}