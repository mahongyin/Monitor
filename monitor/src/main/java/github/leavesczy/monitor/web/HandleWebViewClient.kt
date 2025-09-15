package github.leavesczy.monitor.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.KeyEvent
import android.webkit.ClientCertRequest
import android.webkit.HttpAuthHandler
import android.webkit.RenderProcessGoneDetail
import android.webkit.SafeBrowsingResponse
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi


/**
 * Created By Mahongyin
 * Date    2025/9/12 10:58
 *
 */
object HandleWebViewClient {
    private fun injectVConsole(): String {
        val console = "https://unpkg.com/vconsole@3.14.6/dist/vconsole.min.js"
        val jsFun =
            "javascript:(function(){ " +
                    "if (typeof window.vConsole !== 'undefined' && vConsole instanceof Object) { " +
                    " console.log('vConsole已添加');" +
                    "} else {" +
                    "console.log('vConsole去添加');" +
                    "if(document.head && !document.getElementById('v_console')) {" +
                    "var injectScript = document.createElement('script');" +
                    "injectScript.src='" + console + "';" +
                    "injectScript.id='v_console';" +
                    "injectScript.type='text/javascript';" +
                    "injectScript.onload=function() {" +
                    "let vConsole = new VConsole();" +
                    "console.log('vConsole实例化成功'); " +
                    "};" +
                    "document.head.appendChild(injectScript);" +
                    "} " +
                    "};" +
                    "})();"
        return jsFun
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun handleWebViewClient(webView: WebView?, client: WebViewClient?): WebViewClient {
        webView?.settings?.javaScriptEnabled = true
        return object : WebViewClient() {
            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                client?.onReceivedSslError(view, handler, error) ?: handler?.proceed()//TODO 忽略了证书错误
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (client != null) {
                    return client.shouldOverrideUrlLoading(view, url)
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (client != null) {
                    return client.shouldOverrideUrlLoading(view, request)
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                client?.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view?.evaluateJavascript(injectVConsole(), null)
                } else {
                    view?.loadUrl(injectVConsole())
                }
                client?.onPageFinished(view, url)
            }

            //在 API >= 21 的设备上优先被调用 后台线程调用，严禁在此方法中直接进行UI操作
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                if (client != null) {
                    return client.shouldInterceptRequest(view, request)
                }
                return super.shouldInterceptRequest(view, request)
            }

            //在 API < 21 的设备上被调用 后台线程调用，严禁在此方法中直接进行UI操作
            override fun shouldInterceptRequest(
                view: WebView?,
                url: String?
            ): WebResourceResponse? {
                if (client != null) {
                    return client.shouldInterceptRequest(view, url)
                }
                return super.shouldInterceptRequest(view, url)
            }

            override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
                if (client != null) {
                    return client.shouldOverrideKeyEvent(view, event)
                }
                return super.shouldOverrideKeyEvent(view, event)
            }

            override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
                client?.onScaleChanged(view, oldScale, newScale)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                client?.onReceivedError(view, errorCode, description, failingUrl)
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                client?.onReceivedError(view, request, error)
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                client?.doUpdateVisitedHistory(view, url, isReload)
            }

            override fun onFormResubmission(
                view: WebView?,
                dontResend: Message?,
                resend: Message?
            ) {
                client?.onFormResubmission(view, dontResend, resend)
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                client?.onLoadResource(view, url)
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                client?.onPageCommitVisible(view, url)
            }


            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
                client?.onReceivedClientCertRequest(view, request)
            }

            override fun onReceivedHttpAuthRequest(
                view: WebView?,
                handler: HttpAuthHandler?,
                host: String?,
                realm: String?
            ) {
                client?.onReceivedHttpAuthRequest(view, handler, host, realm)
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                client?.onReceivedHttpError(view, request, errorResponse)
            }

            override fun onReceivedLoginRequest(
                view: WebView?,
                realm: String?,
                account: String?,
                args: String?
            ) {
                client?.onReceivedLoginRequest(view, realm, account, args)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                if (client != null) {
                    return client.onRenderProcessGone(view, detail)
                }
                return super.onRenderProcessGone(view, detail)
            }

            @RequiresApi(Build.VERSION_CODES.O_MR1)
            override fun onSafeBrowsingHit(
                view: WebView?,
                request: WebResourceRequest?,
                threatType: Int,
                callback: SafeBrowsingResponse?
            ) {
                client?.onSafeBrowsingHit(view, request, threatType, callback)
            }

            override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
                client?.onUnhandledKeyEvent(view, event)
            }

            override fun onTooManyRedirects(
                view: WebView?,
                cancelMsg: Message?,
                continueMsg: Message?
            ) {
                client?.onTooManyRedirects(view, cancelMsg, continueMsg)
            }

            override fun equals(other: Any?): Boolean {
                if (client != null) {
                    return client.equals(other)
                }
                return super.equals(other)
            }

            override fun hashCode(): Int {
                if (client != null) {
                    return client.hashCode()
                }
                return super.hashCode()
            }

            override fun toString(): String {
                if (client != null) {
                    return client.toString()
                }
                return super.toString()
            }


        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun handleWebViewClient(webView: com.tencent.smtt.sdk.WebView?, client: com.tencent.smtt.sdk.WebViewClient?): com.tencent.smtt.sdk.WebViewClient {
        webView?.settings?.javaScriptEnabled = true
        return object : com.tencent.smtt.sdk.WebViewClient() {
            override fun doUpdateVisitedHistory(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: String?,
                p2: Boolean
            ) {
                client?.doUpdateVisitedHistory(p0, p1, p2)
            }

            override fun onDetectedBlankScreen(p0: String?, p1: Int) {
                client?.onDetectedBlankScreen(p0, p1)
            }

            override fun onFormResubmission(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: Message?,
                p2: Message?
            ) {
                client?.onFormResubmission(p0, p1, p2)
            }

            override fun onLoadResource(p0: com.tencent.smtt.sdk.WebView?, p1: String?) {
                client?.onLoadResource(p0, p1)
            }

            override fun onPageCommitVisible(p0: com.tencent.smtt.sdk.WebView?, p1: String?) {
                client?.onPageCommitVisible(p0, p1)
            }

            override fun onPageFinished(p0: com.tencent.smtt.sdk.WebView?, p1: String?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    p0?.evaluateJavascript(injectVConsole(), null)
                } else {
                    p0?.loadUrl(injectVConsole())
                }
                client?.onPageFinished(p0, p1)
            }

            override fun onPageStarted(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: String?,
                p2: Bitmap?
            ) {
                client?.onPageStarted(p0, p1, p2)
            }

            override fun onReceivedClientCertRequest(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: com.tencent.smtt.export.external.interfaces.ClientCertRequest?
            ) {
                client?.onReceivedClientCertRequest(p0, p1)
            }

            override fun onReceivedError(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: Int,
                p2: String?,
                p3: String?
            ) {
                client?.onReceivedError(p0, p1, p2, p3)
            }

            override fun onReceivedError(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: com.tencent.smtt.export.external.interfaces.WebResourceRequest?,
                p2: com.tencent.smtt.export.external.interfaces.WebResourceError?
            ) {
                client?.onReceivedError(p0, p1, p2)
            }

            override fun onReceivedHttpAuthRequest(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: com.tencent.smtt.export.external.interfaces.HttpAuthHandler?,
                p2: String?,
                p3: String?
            ) {
                client?.onReceivedHttpAuthRequest(p0, p1, p2, p3)
            }

            override fun onReceivedHttpError(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: com.tencent.smtt.export.external.interfaces.WebResourceRequest?,
                p2: com.tencent.smtt.export.external.interfaces.WebResourceResponse?
            ) {
                client?.onReceivedHttpError(p0, p1, p2)
            }

            override fun onReceivedLoginRequest(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: String?,
                p2: String?,
                p3: String?
            ) {
                client?.onReceivedLoginRequest(p0, p1, p2, p3)
            }

            override fun onReceivedSslError(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: com.tencent.smtt.export.external.interfaces.SslErrorHandler?,
                p2: com.tencent.smtt.export.external.interfaces.SslError?
            ) {
                client?.onReceivedSslError(p0, p1, p2) ?: p1?.proceed()
            }

            override fun onRenderProcessGone(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: com.tencent.smtt.sdk.WebViewClient.RenderProcessGoneDetail?
            ): Boolean {
                if (client != null) {
                    return client.onRenderProcessGone(p0, p1)
                }
                return super.onRenderProcessGone(p0, p1)
            }

            override fun onScaleChanged(p0: com.tencent.smtt.sdk.WebView?, p1: Float, p2: Float) {
                client?.onScaleChanged(p0, p1, p2)
            }

            override fun onUnhandledKeyEvent(p0: com.tencent.smtt.sdk.WebView?, p1: KeyEvent?) {
                client?.onUnhandledKeyEvent(p0, p1)
            }

            override fun shouldInterceptRequest(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: String?
            ): com.tencent.smtt.export.external.interfaces.WebResourceResponse? {
                return client?.shouldInterceptRequest(p0, p1)
            }

            override fun shouldInterceptRequest(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: com.tencent.smtt.export.external.interfaces.WebResourceRequest?
            ): com.tencent.smtt.export.external.interfaces.WebResourceResponse? {
                return client?.shouldInterceptRequest(p0, p1)
            }

            override fun shouldInterceptRequest(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: com.tencent.smtt.export.external.interfaces.WebResourceRequest?,
                p2: Bundle?
            ): com.tencent.smtt.export.external.interfaces.WebResourceResponse? {
                if (client != null) {
                    return client.shouldInterceptRequest(p0, p1, p2)
                }
                return client?.shouldInterceptRequest(p0, p1, p2)
            }

            override fun shouldOverrideKeyEvent(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: KeyEvent?
            ): Boolean {
                if (client != null) {
                    return client.shouldOverrideKeyEvent(p0, p1)
                }
                return super.shouldOverrideKeyEvent(p0, p1)
            }

            override fun shouldOverrideUrlLoading(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: String?
            ): Boolean {
                if (client != null) {
                    return client.shouldOverrideUrlLoading(p0, p1)
                }
                return super.shouldOverrideUrlLoading(p0, p1)
            }

            override fun shouldOverrideUrlLoading(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: com.tencent.smtt.export.external.interfaces.WebResourceRequest?
            ): Boolean {
                if (client != null) {
                    return client.shouldOverrideUrlLoading(p0, p1)
                }
                return super.shouldOverrideUrlLoading(p0, p1)
            }

            override fun onTooManyRedirects(
                p0: com.tencent.smtt.sdk.WebView?,
                p1: Message?,
                p2: Message?
            ) {
                client?.onTooManyRedirects(p0, p1, p2)
            }

            override fun equals(other: Any?): Boolean {
                if (client != null) {
                    return client.equals(other)
                }
                return super.equals(other)
            }

            override fun hashCode(): Int {
                if (client != null) {
                    return client.hashCode()
                }
                return super.hashCode()
            }

            override fun toString(): String {
                if (client != null) {
                    return client.toString()
                }
                return super.toString()
            }
        }
    }
}