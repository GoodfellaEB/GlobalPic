package com.fufu.epicture.listeners

import com.fufu.epicture.imgur.AccessToken

/**
 * Created by weryp on 2/11/18.
 */
interface FragmentsListener {

    fun onAuthorizationTokenReceived(accessToken: AccessToken)
    fun onImageUploaded()
    fun onLogout()

}