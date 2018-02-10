package com.fufu.epicture.listeners

import com.fufu.epicture.imgur.AccessToken

/**
 * Created by weryp on 2/7/18.
 */

interface AuthorizationTokenReceivedListener {

    fun onAuthorizationTokenReceived(accessToken: AccessToken)
}