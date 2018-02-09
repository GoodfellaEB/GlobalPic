package com.fufu.epicture

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import com.google.gson.Gson
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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("DEBUG", "onCreateView")
        homeView = inflater?.inflate(R.layout.home_layout, container, false)
        return (homeView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        resolveAccessToken()
        imgurRequests = ImgurRequests(this)
        gridView = homeView?.findViewById<GridView>(R.id.gridview)
        gridView?.adapter = adapter
        adapter.add(EpictureImage("https://cloud.netlifyusercontent.com/assets/344dbf88-fdf9-42bb-adb4-46f01eedd629/68dd54ca-60cf-4ef7-898b-26d7cbe48ec7/10-dithering-opt.jpg"))
        ImageLoader(adapter.getItem(adapter.count - 1), this).execute(adapter.getItem(adapter.count - 1).getLink())
        adapter.add(EpictureImage("https://cdn.pixabay.com/photo/2013/04/06/11/50/image-editing-101040_960_720.jpg"))
        ImageLoader(adapter.getItem(adapter.count - 1), this).execute(adapter.getItem(adapter.count - 1).getLink())
        imgurRequests.accountImages(accessToken)
    }

    override fun onImageLoaded() {
        adapter.notifyDataSetChanged()
    }

    private fun resolveAccessToken() {
        accessToken = Gson().fromJson(arguments.getString("accessToken"), AccessToken::class.java)
    }

    /*
        Implementation RequestHandler Interface
     */

    override fun onRefreshTokenResponse(response: Response) {

    }

    override fun onAccountImagesResponse(response: Response) {
        Log.d("DEBUG", "onAccountImagesResponse")
    }
}