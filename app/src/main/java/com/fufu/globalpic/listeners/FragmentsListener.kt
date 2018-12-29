package com.fufu.globalpic.listeners

import com.fufu.globalpic.imgur.AccessToken

interface FragmentsListener {

    fun onAuthorizationTokenReceived(accessToken: AccessToken)
    fun onImageUploaded()
    fun onLogout()

}