package com.fufu.epicture

import android.app.Fragment
import android.net.Uri
import android.util.Log
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList




/**
 * Created by weryp on 2/8/18.
 */
class ImgurRequests(handler: RequestHandler) {

    private val _handler = handler

    companion object {
        private const val SCHEME : String = "https"
        private const val AUTHORITY : String = "api.imgur.com"

        private const val REFRESH_TOKEN : String = "oauth2/token"
        private const val ACCOUNT_IMAGES : String = "3/account/me/images"
    }

    private val queueRequest : Queue<DataRequest> = LinkedList<DataRequest>()

    enum class RequestType {
        GET,
        POST
    }

    private class DataRequest {
        var request : RequestHandler.Type = RequestHandler.Type.TOKEN_REFRESH
        var url : String = ""
        var requestType : RequestType = RequestType.GET
        var headers : ArrayList<Pair<String, String>>? = null
        var body : String = "{}"
    }

    fun refreshToken(accessToken: AccessToken) {
        val uriBuilder = getBaseUri()
        val body = JsonObject()
        val dataRequest = DataRequest()

        uriBuilder.appendPath(REFRESH_TOKEN)
        body.addProperty("refresh_token", accessToken.getRefreshToken())
        body.addProperty("client_id", ImgurAppData.CLIENT_ID)
        body.addProperty("client_secret", ImgurAppData.CLIENT_SECRET)
        body.addProperty("grant_type", "refresh_token")
        dataRequest.request = RequestHandler.Type.TOKEN_REFRESH
        dataRequest.requestType = RequestType.POST
        dataRequest.url = uriBuilder.build().toString()
        dataRequest.body = body.toString()
        sendRequest(dataRequest)
    }

    fun accountImages(accessToken: AccessToken) {
        val uriBuilder = getBaseUri()
        val headers : ArrayList<Pair<String, String>> = ArrayList()
        val dataRequest = DataRequest()

        uriBuilder.appendPath(ACCOUNT_IMAGES)
        headers.add(Pair("Authorization", "Bearer " + accessToken.getAccessToken()))
        dataRequest.request = RequestHandler.Type.ACCOUNT_IMAGES
        dataRequest.url = uriBuilder.build().toString()
        dataRequest.headers = headers
        sendRequest(dataRequest)
    }

    private fun getBaseUri() : Uri.Builder {
        val uriBuilder = Uri.Builder()

        return (uriBuilder.scheme(SCHEME).authority(AUTHORITY))
    }

    private fun sendRequest(dataRequest: DataRequest) {
        val okHttpClient = OkHttpClient()
        val request : okhttp3.Request = createRequest(dataRequest)

        queueRequest.add(dataRequest)
        okHttpClient.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call?, response: Response?) {
                Log.d("DEBUG", "onResponse")

                (_handler as? MainActivity)?.runOnUiThread({ sendResponse(response) })
                (_handler as? Fragment)?.activity?.runOnUiThread({ sendResponse(response) })
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("DEBUG", "onFailure")
            }
        })
    }

    private fun createRequest(dataRequest: DataRequest)
            : okhttp3.Request {
        val builder : okhttp3.Request.Builder = okhttp3.Request.Builder().url(dataRequest.url)
        val headers = dataRequest.headers

        if (headers != null) {
            for (header in headers) {
                builder.addHeader(header.first, header.second)
            }
        }
        if (dataRequest.requestType == RequestType.POST) {
            val jsonEncode = MediaType.parse("application/json; charset=utf-8")
            val jsonContent = dataRequest.body

            builder.post(RequestBody.create(jsonEncode, jsonContent))
        }
        return (builder.build())
    }

    private fun sendResponse(response: Response?) {
        val dataRequest : DataRequest = queueRequest.remove()

        if (response?.code() == 200) {
            Log.d("DEBUG", "valid response")
            when (dataRequest.request) {
                RequestHandler.Type.TOKEN_REFRESH -> _handler.onRefreshTokenResponse(response)
                RequestHandler.Type.ACCOUNT_IMAGES -> _handler.onAccountImagesResponse(response)
            }
        }
    }
}