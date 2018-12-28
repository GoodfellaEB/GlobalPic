package com.fufu.globalpic.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.*
import com.bumptech.glide.Glide
import com.fufu.globalpic.R
import com.fufu.globalpic.dataBase.FavoritesDBHandler
import com.fufu.globalpic.imgur.AccessToken
import com.fufu.globalpic.imgur.ImgurAppData
import com.fufu.globalpic.imgur.ImgurRequests
import com.fufu.globalpic.imgur.RequestHandler
import com.fufu.globalpic.listeners.FragmentsListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.Response
import org.apache.commons.io.IOUtils
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*


class AddFragment : Fragment(), RequestHandler {

    private var dbHandler : FavoritesDBHandler? = null
    private val favoritesQueue : Queue<Boolean> = LinkedList<Boolean>()

    private lateinit var addView : View
    private lateinit var  imageButton : ImageButton
    private lateinit var button : Button
    private lateinit var title : EditText
    private lateinit var description : EditText
    private lateinit var favoriteCheckBox : CheckBox
    private var imageUri : Uri = Uri.parse("")

    private lateinit var imgurRequests : ImgurRequests

    private lateinit var accessToken : AccessToken

    companion object {
        private const val PICK_IMAGE : Int = 1
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context != null)
            dbHandler = FavoritesDBHandler(context, ImgurAppData.DB_NAME)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.only_logout, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.logout) {
            if (activity is FragmentsListener)
                (activity as FragmentsListener).onLogout()
        }

        return when (item?.itemId) {
            R.id.logout -> true
            else -> super.onOptionsItemSelected(item)
        }
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
        imageButton = addView.findViewById(R.id.add_image_button)
        imageButton.setOnClickListener({ onImageClick() })
        button = addView.findViewById(R.id.add_submit_button)
        button.setOnClickListener({ onSubmitClick() })
        title = addView.findViewById(R.id.image_title)
        description = addView.findViewById(R.id.image_description)
        favoriteCheckBox = addView.findViewById(R.id.favorite_check_box)
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
            favoritesQueue.add(favoriteCheckBox.isChecked)
            imgurRequests.imageUpload(bytes, title.text.toString(),
                    description.text.toString(), accessToken)
            title.text.clear()
            description.text.clear()
            favoriteCheckBox.isChecked = false
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
        val jsonTree = JsonParser().parse(response.body()?.string())
        val jsonObject : JsonObject

        if (favoritesQueue.remove() && jsonTree.isJsonObject) {
            jsonObject = jsonTree.asJsonObject.getAsJsonObject("data")
            if (jsonObject.isJsonObject && jsonObject.get("id").isJsonNull.not()) {
                dbHandler?.addFavorite(jsonObject.get("id").asString)
            }
        }
        toastMessage(activity?.getString(R.string.toast_image_uploaded))
        if (activity is FragmentsListener)
            (activity as FragmentsListener).onImageUploaded()
    }

    override fun onImageUploadFail() {
        favoritesQueue.remove()
    }
}