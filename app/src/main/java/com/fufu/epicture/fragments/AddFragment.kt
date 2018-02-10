package com.fufu.epicture.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageButton
import com.fufu.epicture.MainActivity
import com.fufu.epicture.R
import com.fufu.epicture.imgur.AccessToken
import com.fufu.epicture.imgur.ImgurRequests
import com.fufu.epicture.imgur.RequestHandler
import com.google.gson.Gson
import okhttp3.Response
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Created by weryp on 2/10/18.
 */

class AddFragment : Fragment(), RequestHandler {

    private var addView : View? = null
    private var imageButton : ImageButton? = null
    private lateinit var imgurRequests : ImgurRequests

    private lateinit var accessToken : AccessToken

    companion object {
        private const val PICK_IMAGE : Int = 1
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        addView = inflater.inflate(R.layout.add_layout, container, false)
        return (addView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        resolveAccessToken()
        imgurRequests = ImgurRequests(this)
        imageButton = addView?.findViewById<ImageButton>(R.id.add_image_button)
        imageButton?.setOnClickListener({ onImageClick() })
    }

    private fun resolveAccessToken() {
        val accessTokenString : String
        val bundle : Bundle? = arguments

        if (bundle != null) {
            accessTokenString = bundle.getString("accessToken")
            accessToken = Gson().fromJson(accessTokenString , AccessToken::class.java)
        }
    }

    private fun onImageClick() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(intent, AddFragment.PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK
                && data != null && data.data != null) {
            val bitmap : Bitmap = data.extras.get("data") as Bitmap
            val stream = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            
            Log.d("DEBUG","result : " +  data.dataString)
        }
    }

    override fun onRefreshTokenResponse(response: Response) {

    }

    override fun onAccountImagesResponse(response: Response) {

    }
}