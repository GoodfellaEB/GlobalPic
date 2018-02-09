package com.fufu.epicture

import android.graphics.Bitmap

/**
 * Created by weryp on 2/8/18.
 */
class EpictureImage(link: String) {
    private var _link = link
    var bitmap : Bitmap? = null

    fun getLink() : String {
        return (_link)
    }

    fun setLink(link: String) {
        _link = link
    }
}