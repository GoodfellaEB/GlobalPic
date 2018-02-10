package com.fufu.epicture.display


/**
 * Created by weryp on 2/8/18.
 */

class EpictureImage(id: String, link: String, title: String?, description: String?) {
    private val _id = id
    private val _title = title
    private val _description = description
    private val _link = link

    fun getId() : String {
        return (_id)
    }

    fun getLink() : String {
        return (_link)
    }

    fun getTitle() : String? {
        return (_title)
    }

    fun getDescription() : String? {
        return (_description)
    }
}