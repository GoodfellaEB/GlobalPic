package com.fufu.globalpic.fragments

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.*
import com.fufu.globalpic.*
import com.fufu.globalpic.dataBase.FavoritesDBHandler
import com.fufu.globalpic.display.GlobalPicImage
import com.fufu.globalpic.display.ImageAdapter
import com.fufu.globalpic.imgur.AccessToken
import com.fufu.globalpic.imgur.ImgurAppData
import com.fufu.globalpic.imgur.ImgurRequests
import com.fufu.globalpic.imgur.RequestHandler
import com.fufu.globalpic.listeners.FragmentsListener
import com.fufu.globalpic.listeners.ImageLoadListener
import com.google.gson.*
import okhttp3.Response

class HomeFragment : Fragment(), ImageLoadListener,
        RequestHandler, AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {

    private var fragmentType : FragmentType = FragmentType.NORMAL

    private lateinit var searchView : SearchView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.options_menu, menu)
        if (menu != null) {
            searchView = menu.findItem(R.id.search)?.actionView as SearchView
            searchView.setOnQueryTextListener(this)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> if (activity is FragmentsListener)
                (activity as FragmentsListener).onLogout()
            R.id.display_all -> adapter.setFilterType(ImageAdapter.FilterType.ALL)
            R.id.with_title -> adapter.setFilterType(ImageAdapter.FilterType.WITH_TITLE)
            R.id.with_description -> adapter.setFilterType(ImageAdapter.FilterType.WITH_DESCRIPTION)
        }

            adapter.filter.filter("")
        return when (item?.itemId) {
            R.id.logout -> true
            R.id.display_all -> true
            R.id.with_title -> true
            R.id.with_description -> true
            else -> super.onOptionsItemSelected(item)
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

    override fun onQueryTextChange(newText: String?): Boolean {
        Log.d("DEBUG", "onQueryTextChange")
        if (newText != null) {
            adapter.setFilter(newText)
            adapter.filter.filter("")
        }
        return (true)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        Log.d("DEBUG", "onQueryTextSubmit")
        searchView.clearFocus()
        return (true)
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
            adapter.add(GlobalPicImage(imageId.asString, imageUrl.asString, title, description))
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
        val globalpicImage = adapter.getCurrentImages()[itemZoomed]

        zoomView.visibility = View.GONE
        if (isFavorite(globalpicImage) != zoomCheckBox.isChecked) {
            if (zoomCheckBox.isChecked)
                handler?.addFavorite(globalpicImage.getId())
            else
                handler?.deleteFavorite(globalpicImage.getId())
            loadFavorites()
        }
        zoomCheckBox.isChecked = false
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val layout = gridView.getChildAt(position - gridView.firstVisiblePosition)
        val imageView : ImageView = layout.findViewById(R.id.image_view)
        val globalpicImage = adapter.getCurrentImages()[position]

        resizeZoomImage()
        zoomView.visibility = View.VISIBLE
        zoomImage.setImageDrawable(imageView.drawable)
        zoomTitle.text = if (globalpicImage.getTitle() != null)
            globalpicImage.getTitle() else ""
        zoomDescription.text = if (globalpicImage.getDescription() != null)
            globalpicImage.getDescription() else ""
        itemZoomed = position
        zoomCheckBox.isChecked = isFavorite(globalpicImage)
    }

    private fun resizeZoomImage() {
        val params : ViewGroup.LayoutParams = zoomImageLayout.layoutParams

        params.width = gridView.columnWidth * 2
        params.height = gridView.columnWidth * 2
        zoomImageLayout.layoutParams = params
        zoomImageLayout.requestLayout()
    }

    private fun isFavorite(globalpicImage: GlobalPicImage) : Boolean {
        return if (favorites.any { it == globalpicImage.getId() }) (true) else (false)
    }

    private fun reloadFilters() {
        if (fragmentType == FragmentType.FAVORITE) {
            adapter.setListViewsToDisplay(favorites)
            adapter.filter.filter("")
        }
    }
}