package com.fufu.epicture.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.fufu.epicture.R
import com.fufu.epicture.imgur.AccessToken
import com.fufu.epicture.imgur.ImgurRequests
import com.fufu.epicture.imgur.RequestHandler
import com.google.gson.Gson
import okhttp3.Response
import org.apache.commons.io.IOUtils
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * Created by weryp on 2/10/18.
 */

class AddFragment : Fragment(), RequestHandler {

    private lateinit var addView : View
    private lateinit var  imageButton : ImageButton
    private lateinit var button : Button
    private lateinit var title : EditText
    private lateinit var description : EditText
    private var imageUri : Uri = Uri.parse("")

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
        imageButton = addView.findViewById<ImageButton>(R.id.add_image_button)
        imageButton.setOnClickListener({ onImageClick() })
        button = addView.findViewById<Button>(R.id.add_submit_button)
        button.setOnClickListener({ onSubmitClick() })
        title = addView.findViewById<EditText>(R.id.image_title)
        description = addView.findViewById<EditText>(R.id.image_description)
    }

    private fun resolveAccessToken() {
        val accessTokenString : String
        val bundle : Bundle? = arguments

        if (bundle != null) {
            accessTokenString = bundle.getString("accessToken")
            accessToken = Gson().fromJson(accessTokenString , AccessToken::class.java)
        }
    }

    private fun onSubmitClick() {
        val bytes : ByteArray? = getImageBytes(imageUri)

        if (bytes != null && bytes.count() > 0) {
            toastMessage(activity?.getString(R.string.toast_image_submit))
            imgurRequests.imageUpload(bytes, title.text.toString(),
                    description.text.toString(), accessToken)
            title.text.clear()
            description.text.clear()
        } else
            toastMessage(activity?.getString(R.string.toast_no_selected_image))
    }

    private fun onImageClick() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(intent, AddFragment.PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK
                && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(addView).load(imageUri).into(imageButton)
        }
    }

    private fun getImageBytes(uri: Uri) : ByteArray? {
        val inputStream : InputStream?
        var bytes : ByteArray? = null

        try {
            inputStream = activity?.contentResolver?.openInputStream(uri)
            bytes = IOUtils.toByteArray(inputStream)
        } catch (e: FileNotFoundException) {
            Log.d("DEBUG", "file not found : " + e.toString())
        }
        return (bytes)
    }

    private fun toastMessage(message: String?) {
        if (message != null)
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRefreshTokenResponse(response: Response) {

    }

    override fun onAccountImagesResponse(response: Response) {

    }

    override fun onImageUploadResponse(response: Response) {
        toastMessage(activity?.getString(R.string.toast_image_uploaded))
    }
}