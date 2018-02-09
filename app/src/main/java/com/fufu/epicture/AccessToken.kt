package com.fufu.epicture

import android.net.Uri
import android.util.Log
import com.google.gson.JsonObject
import java.util.*

/**
 * Created by weryp on 2/7/18.
 */
class AccessToken (fragment: String) {

    companion object {
        private const val TIME_MARGIN = 10
    }

    private var accessToken : String
    private var tokenType : String
    private var expiresIn : String
    private var refreshToken : String
    private val creationDate : Date

    init {
        val fragmentQuery : Uri = Uri.parse("?" + fragment)
        var parameter : String?

        parameter = fragmentQuery.getQueryParameter("access_token")
        accessToken = if (parameter != null) parameter else ""
        parameter = fragmentQuery.getQueryParameter("token_type")
        tokenType = if (parameter != null) parameter else ""
        parameter = fragmentQuery.getQueryParameter("expires_in")
        expiresIn = if (parameter != null) parameter else ""
        parameter = fragmentQuery.getQueryParameter("refresh_token")
        refreshToken = if (parameter != null) parameter else ""
        creationDate = Date()
    }

    fun parseJsonObject(jsonObject: JsonObject) {
        accessToken = jsonObject.get("access_token").asString
        tokenType = jsonObject.get("token_type").asString
        expiresIn = jsonObject.get("expires_in").asString
        refreshToken = jsonObject.get("refresh_token").asString
    }

    fun isExpired() : Boolean {
        val limitDate : Long = creationDate.time + (expiresIn.toLong() - TIME_MARGIN)

        return (Date(limitDate).before(Date()))
    }

    fun getAccessToken() : String {
        return (accessToken)
    }

    fun getTokenType() : String {
        return (tokenType)
    }

    fun getRefreshToken() : String {
        return (refreshToken)
    }
}