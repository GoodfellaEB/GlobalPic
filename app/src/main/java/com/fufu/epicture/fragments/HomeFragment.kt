package com.fufu.epicture.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import com.fufu.epicture.*
import com.fufu.epicture.display.EpictureImage
import com.fufu.epicture.display.ImageAdapter
import com.fufu.epicture.imgur.AccessToken
import com.fufu.epicture.imgur.ImgurRequests
import com.fufu.epicture.imgur.RequestHandler
import com.fufu.epicture.listeners.ImageLoadListener
import com.google.gson.*
import okhttp3.Response

/**
 * Created by weryp on 2/8/18.
 */

class HomeFragment : Fragment(), ImageLoadListener, RequestHandler {

    private var homeView : View? = null
    private var gridView : GridView? = null
    private lateinit var adapter : ImageAdapter
    private lateinit var imgurRequests : ImgurRequests

    private lateinit var accessToken : AccessToken

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context != null)
            adapter = ImageAdapter(context, R.layout.grid_view_item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        homeView = inflater.inflate(R.layout.home_layout, container, false)
        return (homeView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        resolveAccessToken()
        imgurRequests = ImgurRequests(this)
        gridView = homeView?.findViewById<GridView>(R.id.gridview)
        gridView?.adapter = adapter
        imgurRequests.accountImages(accessToken)
    }

    override fun onImageLoaded() {
        synchronized(this) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun resolveAccessToken() {
        val accessTokenString : String
        val bundle : Bundle? = arguments

        if (bundle != null) {
            accessTokenString = bundle.getString("accessToken")
            accessToken = Gson().fromJson(accessTokenString , AccessToken::class.java)
        }
    }

    /*
        Implementation RequestHandler Interface
     */

    override fun onRefreshTokenResponse(response: Response) {

    }

    override fun onAccountImagesResponse(response: Response) {
        Log.d("DEBUG", "onAccountImagesResponse")

        val jsonTree = JsonParser().parse(response.body()?.string())

        if (jsonTree.isJsonObject)
            parseAccountImagesData(jsonTree.asJsonObject.getAsJsonArray("data"))
        else
            Log.d("DEBUG", "json format error -> object expected")
    }

    private fun parseAccountImagesData(jsonArray: JsonArray) {
        var imageId : JsonElement?
        var imageUrl : JsonElement?
        var imageTitle : JsonElement?
        var imageDescription : JsonElement?
        var title : String?
        var description : String?

        for (imageObject in jsonArray) {
            if (imageObject.isJsonObject) {
                imageId = imageObject.asJsonObject.get("id")
                imageUrl = imageObject.asJsonObject.get("link")
                imageTitle = imageObject.asJsonObject.get("title")
                imageDescription = imageObject.asJsonObject.get("description")
                title = if (imageTitle.isJsonNull) "" else imageTitle?.asString
                description = if (imageTitle.isJsonNull) "" else imageDescription?.asString
                if (isNewImage(imageId, imageUrl))
                    adapter.add(EpictureImage(imageId.asString, imageUrl.asString, title, description))
            }
        }
    }

    private fun isNewImage(imageId: JsonElement?, imageUrl: JsonElement?) : Boolean{
        var id = 0

        if (imageId != null && imageUrl != null) {
            while (id < adapter.count) {
                if (adapter.getItem(id).getId() == imageId.asString)
                    return (false)
                ++id
            }
            return (true)
        }
        return (false)
    }
}