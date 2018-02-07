package com.fufu.epicture

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient



/**
 * Created by weryp on 2/6/18.
 */
class LoginFragment : Fragment() {

    private lateinit var tokenListener : AuthorizationTokenReceivedListener
    private lateinit var loginWebView : WebView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("DEBUG", "onCreateView")
        return (inflater?.inflate(R.layout.login_page, container, false))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loginWebView = activity.findViewById(R.id.imgur_webview)

        loginWebView.settings.javaScriptEnabled = true
        loginWebView.webViewClient = LoginWebClient(this)
        loginWebView.loadUrl(getAuthenticateUrl())
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is AuthorizationTokenReceivedListener)
            tokenListener = context
        else
            throw RuntimeException(context.toString()
                    + " context not implement AuthorizationTokenReceivedListener interface")
    }

    override fun onDetach() {
        super.onDetach()
        clearWebView()
        loginWebView.destroy()

        Log.d("DEBUG", "onDetach")
    }

    private fun clearWebView() {
        loginWebView.clearCache(true)
        loginWebView.clearHistory()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    private fun getAuthenticateUrl() : String {
        val uriBuilder = Uri.Builder()

        uriBuilder.scheme("https")
                .authority(ImgurAppData.AUTHORITY)
                .appendPath(ImgurAppData.SECURITY)
                .appendPath(ImgurAppData.AUTHORIZE_REQUEST)
                .appendQueryParameter("client_id", ImgurAppData.CLIENT_ID)
                .appendQueryParameter("redirect_uri", ImgurAppData.REDIRECT_URI)
                .appendQueryParameter("response_type", "token")
        return (uriBuilder.build().toString())
    }

    fun onAuthorizationTokenReceived(accessToken: AccessToken) {
        Log.d("DEBUG", "onAuthorizationTokenReceived")
        tokenListener.onAuthorizationTokenReceived(accessToken)
    }

    private class LoginWebClient(parent: LoginFragment) : WebViewClient() {

        private val _parent : LoginFragment = parent

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            if (request != null) {
                val requestUri : Uri = request.url

                Log.d("DEBUG", "request : " + requestUri.toString())
                if (isRedirectRequest(requestUri)) {
                    Log.d("DEBUG", "Redirect request")
                    handleRedirectRequest(view, requestUri)
                } else {
                    Log.d("DEBUG", "No Redirect request")
                }
            }
            return (super.shouldOverrideUrlLoading(view, request))
        }

        private fun isRedirectRequest(uri: Uri) : Boolean {
            val uriWithoutFragment : Uri = uri.buildUpon().fragment("").build()
            val fragment : String = uri.fragment

            return (uriWithoutFragment.toString().equals(ImgurAppData.REDIRECT_URI)
                    && TextUtils.isEmpty(fragment).not())
        }

        private fun handleRedirectRequest(view: WebView?, uri: Uri) {
            val accessToken = AccessToken(uri.fragment)

            view?.stopLoading()
            view?.loadUrl("about:blank")
            _parent.onAuthorizationTokenReceived(accessToken)
        }
    }
}