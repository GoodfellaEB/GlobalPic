package com.fufu.epicture.display

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.fufu.epicture.R
import com.google.gson.JsonSyntaxException


/**
 * Created by weryp on 2/8/18.
 */

class ImageAdapter(context : Context, resource : Int, pItems : ArrayList<EpictureImage> = ArrayList())
    : ArrayAdapter<EpictureImage>(context, resource, pItems) {

    enum class FilterType {
        ALL,
        WITH_TITLE,
        WITH_DESCRIPTION
    }

    private val _inflater : LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val _resource = resource

    private val images = pItems
    private var imagesToDisplay = pItems
    private val customFilters : CustomsFilters = CustomsFilters()
    private val imageFilter : ImageFilter = ImageFilter()


    fun getCurrentImages() : ArrayList<EpictureImage> {
        return (imagesToDisplay)
    }

    /*
        Filter views
     */
    override fun getFilter(): Filter {
        return (imageFilter)
    }

    override fun getCount(): Int {
        return (imagesToDisplay.size)
    }

    /*
    * Render the view
    * */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val gridView : GridView?
        var view = convertView
        val epictureImage : EpictureImage = imagesToDisplay[position]

        if (view == null) {
            view = _inflater.inflate(_resource, null, false)

            if (parent != null) {
                gridView = parent as GridView
                view?.layoutParams = LinearLayout.LayoutParams(gridView.columnWidth, gridView.columnWidth)
            }
        }

        return (manageViewToDisplay(view, epictureImage))
    }

    private fun manageViewToDisplay(view: View?, epictureImage: EpictureImage) : View {
        val imageView : ImageView? = view?.findViewById(R.id.image_view)

        if (view != null && imageView != null)
            Glide.with(view).load(epictureImage.getLink()).into(imageView)
        return (view as View)
    }

    fun setListViewsToDisplay(list: ArrayList<String>) {
        customFilters.listViewsToDisplay = list
    }

    fun setFilter(filter: String) {
        customFilters.filter = filter
    }

    fun setFilterType(filterType: FilterType) {
        customFilters.filterType = filterType
    }

    private class CustomsFilters {
        var listViewsToDisplay = ArrayList<String>()
        var filter = ""
        var filterType = FilterType.ALL

        fun isEmpty() : Boolean {
            return (listViewsToDisplay.isEmpty() && TextUtils.isEmpty(filter)
                    && filterType == FilterType.ALL)
        }
    }

    inner class ImageFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()

            if (customFilters.isEmpty()) {
                results.values = images
                results.count = images.size
            } else {
                val filteredValues: ArrayList<EpictureImage> = ArrayList()

                images.filterTo(filteredValues) { viewNeedToBeDisplayed(it) }
                results.values = filteredValues
                results.count = filteredValues.size
            }

            return (results)
        }

        private fun viewNeedToBeDisplayed(epictureImage: EpictureImage) : Boolean {
            Log.d("DEBUG", "type : " + customFilters.filterType.toString())
            when (customFilters.filterType) {
                FilterType.WITH_TITLE ->
                    if (TextUtils.isEmpty(epictureImage.getTitle())) return (false)
                FilterType.WITH_DESCRIPTION ->
                    if (TextUtils.isEmpty(epictureImage.getDescription())) return (false)
            }
            if (inListViewsToDisplay(epictureImage.getId()).not())
                return (false)
            return (matchWithFilter(epictureImage))
        }

        private fun inListViewsToDisplay(name: String) : Boolean {
            return if (customFilters.listViewsToDisplay.contains(name)) (true)
            else (customFilters.listViewsToDisplay.isEmpty())
        }

        private fun matchWithFilter(epictureImage: EpictureImage) : Boolean {
            val id = epictureImage.getId()
            val name : String = epictureImage.getTitle() ?: id

            return (name.toUpperCase().contains(customFilters.filter.toUpperCase()))
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results?.values != null)
                imagesToDisplay = results.values as ArrayList<EpictureImage>
            notifyDataSetChanged()
        }

    }

}