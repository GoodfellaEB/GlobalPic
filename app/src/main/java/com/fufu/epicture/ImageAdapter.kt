package com.fufu.epicture

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton

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

        imageButton?.setImageBitmap(epictureImage.bitmap)
        return (view as View)
    }
}