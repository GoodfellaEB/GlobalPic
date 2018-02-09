package com.fufu.epicture

import okhttp3.Response

/**
 * Created by weryp on 2/9/18.
 */
interface RequestHandler {
    enum class Type {
        TOKEN_REFRESH,
        ACCOUNT_IMAGES
    }

    fun onRefreshTokenResponse(response: Response)
    fun onAccountImagesResponse(response: Response)
}