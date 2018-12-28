package com.fufu.globalpic.display

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.fufu.globalpic.R
import com.google.gson.JsonSyntaxException

class ImageAdapter(context : Context, resource : Int, pItems : ArrayList<GlobalPicImage> = ArrayList())
    : ArrayAdapter<GlobalPicImage>(context, resource, pItems) {

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


    fun getCurrentImages() : ArrayList<GlobalPicImage> {
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
        val globalpicImage : GlobalPicImage = imagesToDisplay[position]

        if (view == null) {
            view = _inflater.inflate(_resource, null, false)

            if (parent != null) {
                gridView = parent as GridView
                view?.layoutParams = LinearLayout.LayoutParams(gridView.columnWidth, gridView.columnWidth)
            }
        }

        return (manageViewToDisplay(view, globalpicImage))
    }

    private fun manageViewToDisplay(view: View?, globalpicImage: GlobalPicImage) : View {
        val imageView : ImageView? = view?.findViewById(R.id.image_view)

        if (view != null && imageView != null)
            Glide.with(view).load(globalpicImage.getLink()).into(imageView)
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
                val filteredValues: ArrayList<GlobalPicImage> = ArrayList()

                images.filterTo(filteredValues) { viewNeedToBeDisplayed(it) }
                results.values = filteredValues
                results.count = filteredValues.size
            }

            return (results)
        }

        private fun viewNeedToBeDisplayed(globalpicImage: GlobalPicImage) : Boolean {
            when (customFilters.filterType) {
                FilterType.WITH_TITLE ->
                    if (TextUtils.isEmpty(globalpicImage.getTitle())) return (false)
                FilterType.WITH_DESCRIPTION ->
                    if (TextUtils.isEmpty(globalpicImage.getDescription())) return (false)
            }
            if (inListViewsToDisplay(globalpicImage.getId()).not())
                return (false)
            return (matchWithFilter(globalpicImage))
        }

        private fun inListViewsToDisplay(name: String) : Boolean {
            return if (customFilters.listViewsToDisplay.contains(name)) (true)
            else (customFilters.listViewsToDisplay.isEmpty())
        }

        private fun matchWithFilter(globalpicImage: GlobalPicImage) : Boolean {
            val id = globalpicImage.getId()
            val name : String = globalpicImage.getTitle() ?: id

            return (name.toUpperCase().contains(customFilters.filter.toUpperCase()))
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results?.values != null)
                imagesToDisplay = results.values as ArrayList<GlobalPicImage>
            notifyDataSetChanged()
        }

    }

}