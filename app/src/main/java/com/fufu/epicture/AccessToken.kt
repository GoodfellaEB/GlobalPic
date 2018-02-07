package com.fufu.epicture

import android.net.Uri
import java.util.*

/**
 * Created by weryp on 2/7/18.
 */
class AccessToken (fragment: String) {

    private val accessToken : String
    private val tokenType : String
    private val expiresIn : String
    private val refreshToken : String
    private val creationDate : Date

    init {
        val fragmentQuery : Uri = Uri.parse("?" + fragment)

        accessToken = fragmentQuery.getQueryParameter("access_token")
        tokenType = fragmentQuery.getQueryParameter("token_type")
        expiresIn = fragmentQuery.getQueryParameter("expires_in")
        refreshToken = fragmentQuery.getQueryParameter("refresh_token")
        creationDate = Date()
    }

    fun isExpired() : Boolean {
        val limitDate : Long = creationDate.time + expiresIn.toLong()

        return (Date(limitDate).before(Date()))
    }

    fun getAccessToken() : String {
        return (accessToken)
    }

    fun getTokenType() : String {
        return (tokenType)
    }
}