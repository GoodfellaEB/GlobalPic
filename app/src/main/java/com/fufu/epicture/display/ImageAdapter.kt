package com.fufu.epicture.display

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.fufu.epicture.R

/**
 * Created by weryp on 2/8/18.
 */

class ImageAdapter(context : Context, resource : Int, pItems : ArrayList<EpictureImage> = ArrayList())
    : ArrayAdapter<EpictureImage>(context, resource, pItems) {

    private val _inflater : LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val _resource = resource

    /*
    * Render the view
    * */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val epictureImage : EpictureImage = getItem(position)

        if (view == null)
            view = _inflater.inflate(_resource, null, false)

        val imageButton : ImageButton? = view?.findViewById(R.id.imageButton)

        if (view != null && imageButton != null)
            Glide.with(view).load(epictureImage.getLink()).into(imageButton)

        return (view as View)
    }
}