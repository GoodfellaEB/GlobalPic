package com.fufu.epicture.imgur

import android.net.Uri
import android.os.AsyncTask
import android.support.v4.app.Fragment
import android.util.Base64
import android.util.Log
import com.fufu.epicture.MainActivity
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
        private const val IMAGE_UPLOAD : String = "3/image"
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
        var body : JsonObject = JsonObject()
    }

    fun refreshToken(accessToken: AccessToken) {
        val uriBuilder = getBaseUri()
        val dataRequest = DataRequest()

        uriBuilder.appendPath(REFRESH_TOKEN)
        dataRequest.body.addProperty("refresh_token", accessToken.getRefreshToken())
        dataRequest.body.addProperty("client_id", ImgurAppData.CLIENT_ID)
        dataRequest.body.addProperty("client_secret", ImgurAppData.CLIENT_SECRET)
        dataRequest.body.addProperty("grant_type", "refresh_token")
        dataRequest.request = RequestHandler.Type.TOKEN_REFRESH
        dataRequest.requestType = RequestType.POST
        dataRequest.url = uriBuilder.build().toString()
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

    fun imageUpload(bytes : ByteArray, imageTitle: String,
                    imageDescription: String, accessToken: AccessToken) {
        val uriBuilder = getBaseUri()
        val headers : ArrayList<Pair<String, String>> = ArrayList()
        val dataRequest = DataRequest()

        uriBuilder.appendPath(IMAGE_UPLOAD)
        headers.add(Pair("Authorization", "Bearer " + accessToken.getAccessToken()))
        dataRequest.body.addProperty("title", imageTitle)
        dataRequest.body.addProperty("description", imageDescription)
        dataRequest.request = RequestHandler.Type.IMAGE_UPLOAD
        dataRequest.requestType = RequestType.POST
        dataRequest.url = uriBuilder.build().toString()
        dataRequest.headers = headers
        ImageUploader(this, dataRequest).execute(bytes)
    }

    private class ImageUploader(parent : ImgurRequests, dataRequest: DataRequest)
        : AsyncTask<ByteArray, Void, String>() {

        private val _parent = parent
        private val _dataRequest = dataRequest

        override fun doInBackground(vararg params: ByteArray?): String {
            var result = ""

            if (params[0] != null)
                result = Base64.encodeToString(params[0], Base64.DEFAULT)
            return (result)
        }

        override fun onPostExecute(result: String?) {
            _dataRequest.body.addProperty("image", result)
            _parent.sendRequest(_dataRequest)
        }
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
                _handler.onImageUploadFail()
            }
        })
    }

    private fun createRequest(dataRequest: DataRequest)
            : okhttp3.Request {
        val builder : okhttp3.Request.Builder = okhttp3.Request.Builder().url(dataRequest.url)
        val headers = dataRequest.headers

        Log.d("DEBUG", "url : " + dataRequest.url)
        if (headers != null) {
            for (header in headers) {
                builder.addHeader(header.first, header.second)
            }
        }
        if (dataRequest.requestType == RequestType.POST) {
            val jsonEncode = MediaType.parse("application/json; charset=utf-8")
            val jsonContent = dataRequest.body.toString()

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
                RequestHandler.Type.IMAGE_UPLOAD -> _handler.onImageUploadResponse(response)
            }
        } else {
            Log.d("DEBUG", "body : " + response?.body()?.string())
            when (dataRequest.request) {
                RequestHandler.Type.IMAGE_UPLOAD -> _handler.onImageUploadFail()
                else -> Log.d("DEBUG", "fail of " + dataRequest.request.name + " request")
            }
        }
    }
}