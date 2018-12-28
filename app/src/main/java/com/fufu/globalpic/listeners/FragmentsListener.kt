package com.fufu.globalpic.listeners

import com.fufu.globalpic.imgur.AccessToken

/**
 * Created by weryp on 2/11/18.
 */
interface FragmentsListener {

    fun onAuthorizationTokenReceived(accessToken: AccessToken)
    fun onImageUploaded()
    fun onLogout()

}