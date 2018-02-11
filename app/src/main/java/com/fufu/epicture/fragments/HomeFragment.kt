package com.fufu.epicture.fragments

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.fufu.epicture.*
import com.fufu.epicture.dataBase.FavoritesDBHandler
import com.fufu.epicture.display.EpictureImage
import com.fufu.epicture.display.ImageAdapter
import com.fufu.epicture.imgur.AccessToken
import com.fufu.epicture.imgur.ImgurAppData
import com.fufu.epicture.imgur.ImgurRequests
import com.fufu.epicture.imgur.RequestHandler
import com.fufu.epicture.listeners.ImageLoadListener
import com.google.gson.*
import okhttp3.Response
import org.w3c.dom.Text

/**
 * Created by weryp on 2/8/18.
 */

class HomeFragment : Fragment(), ImageLoadListener,
        RequestHandler, AdapterView.OnItemClickListener {

    private var fragmentType : FragmentType = FragmentType.NORMAL

    private var handler : FavoritesDBHandler? = null
    private var favorites : ArrayList<String> = ArrayList()

    private lateinit var homeView : View
    private lateinit var gridView : GridView
    private lateinit var adapter : ImageAdapter
    private lateinit var zoomView : ConstraintLayout
    private lateinit var zoomImageLayout : RelativeLayout
    private lateinit var zoomImage : ImageButton
    private lateinit var zoomTitle : TextView
    private lateinit var zoomDescription : TextView
    private lateinit var zoomCheckBox : CheckBox
    private lateinit var imgurRequests : ImgurRequests
    private var itemZoomed : Int = 0

    private lateinit var accessToken : AccessToken

    override fun onAttach(context: Context?) {
        Log.d("DEBUG", "onAttach")
        super.onAttach(context)

        resolveAccessToken()
        imgurRequests = ImgurRequests(this)
        imgurRequests.accountImages(accessToken)
        if (context != null) {
            adapter = ImageAdapter(context, R.layout.grid_view_item)
            handler = FavoritesDBHandler(context, ImgurAppData.DB_NAME)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d("DEBUG", "onCreateView")
        homeView = inflater.inflate(R.layout.home_layout, container, false)
        return (homeView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d("DEBUG", "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
        resolveFragmentType()

        adapter.setNotifyOnChange(false)
        gridView = homeView.findViewById(R.id.gridView)
        gridView.adapter = adapter
        gridView.onItemClickListener = this
        zoomView = homeView.findViewById(R.id.zoom_view)
        zoomImageLayout = zoomView.findViewById(R.id.image_zoom_layout)
        zoomImage = zoomView.findViewById(R.id.image_zoom)
        zoomTitle = zoomView.findViewById(R.id.zoom_title)
        zoomDescription = zoomView.findViewById(R.id.zoom_description)
        zoomCheckBox = zoomView.findViewById(R.id.zoom_checkBox)
        zoomImage.setOnClickListener({ onZoomButtonClick() })

        loadFavorites()
    }

    private fun loadFavorites() {
        val list : ArrayList<String>? = handler?.getFavorites()

        if (list != null) {
            favorites = list
            reloadFilters()
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

    private fun resolveFragmentType() {
        val fragmentTypeString : String
        val bundle : Bundle? = arguments

        if (bundle != null) {
            fragmentTypeString = bundle.getString("fragmentType")
            fragmentType = Gson().fromJson(fragmentTypeString, FragmentType::class.java)
        }
    }

    override fun onImageLoaded() {
        synchronized(this) {
            adapter.notifyDataSetChanged()
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

    override fun onImageUploadResponse(response: Response) {

    }

    override fun onImageUploadFail() {

    }

    private fun parseAccountImagesData(jsonArray: JsonArray) {
        if (jsonArray.isJsonNull.not()) {
            jsonArray
                    .filter { it.isJsonObject }
                    .forEach { parseAccountImageData(it.asJsonObject) }
        }
    }

    private fun parseAccountImageData(imageObject: JsonObject) {
        val imageId : JsonElement = imageObject.get("id")
        val imageUrl : JsonElement = imageObject.get("link")
        val imageTitle : JsonElement = imageObject.get("title")
        val imageDescription : JsonElement = imageObject.get("description")
        val title : String = if (imageTitle.isJsonNull) "" else imageTitle.asString
        val description : String = if (imageDescription.isJsonNull) "" else imageDescription.asString

        if (isNewImage(imageId, imageUrl)) {
            adapter.add(EpictureImage(imageId.asString, imageUrl.asString, title, description))
            adapter.notifyDataSetChanged()
            reloadFilters()
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

    private fun onZoomButtonClick() {
        val epictureImage = adapter.getCurrentImages()[itemZoomed]

        zoomView.visibility = View.GONE
        if (isFavorite(epictureImage) != zoomCheckBox.isChecked) {
            if (zoomCheckBox.isChecked)
                handler?.addFavorite(epictureImage.getId())
            else
                handler?.deleteFavorite(epictureImage.getId())
            loadFavorites()
        }
        zoomCheckBox.isChecked = false
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val layout = gridView.getChildAt(position - gridView.firstVisiblePosition)
        val imageView : ImageView = layout.findViewById(R.id.image_view)
        val epictureImage = adapter.getCurrentImages()[position]

        resizeZoomImage()
        zoomView.visibility = View.VISIBLE
        zoomImage.setImageDrawable(imageView.drawable)
        zoomTitle.text = if (epictureImage.getTitle() != null)
            epictureImage.getTitle() else ""
        zoomDescription.text = if (epictureImage.getDescription() != null)
            epictureImage.getDescription() else ""
        itemZoomed = position
        zoomCheckBox.isChecked = isFavorite(epictureImage)
    }

    private fun resizeZoomImage() {
        val params : ViewGroup.LayoutParams = zoomImageLayout.layoutParams

        params.width = gridView.columnWidth * 2
        params.height = gridView.columnWidth * 2
        zoomImageLayout.layoutParams = params
        zoomImageLayout.requestLayout()
    }

    private fun isFavorite(epictureImage: EpictureImage) : Boolean {
        return if (favorites.any { it == epictureImage.getId() }) (true) else (false)
    }

    private fun reloadFilters() {
        if (fragmentType == FragmentType.FAVORITE) {
            adapter.setListViewsToDisplay(favorites)
            adapter.filter.filter("")
        }
    }
}