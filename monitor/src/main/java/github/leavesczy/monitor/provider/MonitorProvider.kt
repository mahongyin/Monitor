package github.leavesczy.monitor.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import github.leavesczy.monitor.http.OkHttpConnection
import java.net.Proxy
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


// 提供自动初始化
class MonitorProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val context = context
        if (context == null) {
            Log.e("MonitorProvider", "MonitorProvider初始化context失败")
        } else {
            httpStreamHandler()
            handleSSLHandShake()
        }
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null


    /************************************  URLStreamHandler  ***********************************************/
    //通过动态代理替换 URLStreamHandlerFactory，拦截 URL.openConnection()
    //仅适用于未设置过 URLStreamHandlerFactory 的应用（多数应用未设置）
    fun httpStreamHandler() {
        //注册自定义工厂
        URL.setURLStreamHandlerFactory(object : URLStreamHandlerFactory {
            override fun createURLStreamHandler(protocol: String?): URLStreamHandler? {
                if ("http" == protocol || "https" == protocol) {
                    //URLStreamHandler返回包装后的 HttpsURLConnection，用于拦截数据
                    return object : URLStreamHandler(){
                        override fun openConnection(u: URL?): URLConnection? {
                            return OkHttpConnection(u)
                        }

                        override fun openConnection(u: URL?, p: Proxy?): URLConnection? {
                            return OkHttpConnection(u, p)
                        }
                    }
                }
                return null // 其他协议使用默认实现
            }
        })
    }

    // HttpsURLConnection全局忽略SSL证书
    fun handleSSLHandShake() {
        try {
            val trustManager = object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<out X509Certificate?>? {
                    return arrayOfNulls<X509Certificate>(0)
                }
            }

            val sc: SSLContext = SSLContext.getInstance("TLS")
            // trustAllCerts信任所有的证书
            sc.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
            HttpsURLConnection.setDefaultHostnameVerifier { hostname, session -> true }
        } catch (ignored: Exception) {
            Log.e("MonitorProvider", "handleSSLHandShake: " + ignored.message)
        }
    }
}