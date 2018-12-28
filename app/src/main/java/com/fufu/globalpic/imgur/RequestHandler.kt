package com.fufu.globalpic.imgur

import okhttp3.Response

interface RequestHandler {
    enum class Type {
        TOKEN_REFRESH,
        ACCOUNT_IMAGES,
        IMAGE_UPLOAD
    }

    fun onRefreshTokenResponse(response: Response)
    fun onAccountImagesResponse(response: Response)
    fun onImageUploadResponse(response: Response)
    fun onImageUploadFail()
}