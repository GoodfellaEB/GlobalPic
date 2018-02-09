package com.fufu.epicture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import java.net.URL

/**
 * Created by weryp on 2/8/18.
 */

class ImageLoader(epictureImage: EpictureImage, imageLoadListener: ImageLoadListener)
    : AsyncTask<String, Void, Bitmap>() {

    private val _epictureImage : EpictureImage = epictureImage
    private val _imageLoadListener : ImageLoadListener = imageLoadListener

    override fun doInBackground(vararg p0: String?): Bitmap {
        val url = p0[0]
        var bitmap : Bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)

        try {
            bitmap = BitmapFactory.decodeStream(URL(url).openStream())
        } catch (e : Exception) {
            Log.d("DEBUG", "ImageLoader : " + e.toString())
        }
        return (bitmap)
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        _epictureImage.bitmap = result
        _imageLoadListener.onImageLoaded()
    }
}

