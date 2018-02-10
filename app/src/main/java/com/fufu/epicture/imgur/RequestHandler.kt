package com.fufu.epicture.imgur

import okhttp3.Response

/**
 * Created by weryp on 2/9/18.
 */
interface RequestHandler {
    enum class Type {
        TOKEN_REFRESH,
        ACCOUNT_IMAGES,
        IMAGE_UPLOAD
    }

    fun onRefreshTokenResponse(response: Response)
    fun onAccountImagesResponse(response: Response)
    fun onImageUploadResponse(response: Response)
}