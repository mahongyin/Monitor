package github.leavesczy.monitor.samples

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import github.leavesczy.monitor.MonitorInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @Author: leavesCZY
 * @Date: 2024/3/1 23:24
 * @Desc:
 */
@ExperimentalMaterial3Api
class MainActivity : AppCompatActivity() {
    private var webview: WebView? = null
    private val okHttpClient by lazy(mode = LazyThreadSafetyMode.NONE) {
        OkHttpClient.Builder().apply {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            //addNetworkInterceptor(interceptor = MonitorInterceptor())
        }.build()
    }

    private val apiService by lazy(mode = LazyThreadSafetyMode.NONE) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://httpbin.org")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        retrofit.create(ApiService::class.java)
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            checkNotificationPermission()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonitorSampleTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    modifier = Modifier,
                                    text = "Monitor"
                                )
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues = innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 40.dp),
                            onClick = {
                                networkRequest()
                                showToast(msg = "已发起请求，请查看消息通知栏")
                            }
                        ) {
                            Text(
                                modifier = Modifier,
                                text = "Network Request"
                            )
                        }
                        SimpleWebView()
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            checkNotificationPermission()
        }
    }

    private fun checkNotificationPermission() {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            showToast("请开启消息通知权限，以便查看网络请求")
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun networkRequest() {
        val callback = object : Callback<Void> {
            override fun onFailure(call: Call<Void>, throwable: Throwable) {
                throwable.printStackTrace()
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {

            }
        }
        apiService.get().enqueue(callback)
        apiService.get(code = 404).enqueue(callback)
        apiService.post().enqueue(callback)
        apiService.post(body = Data(random = "posted")).enqueue(callback)
        apiService.put(body = Data(random = "put")).enqueue(callback)
        apiService.delete().enqueue(callback)
        apiService.delay(seconds = 2).enqueue(callback)
        apiService.deny().enqueue(callback)
        apiService.status(code = 304).enqueue(callback)
        apiService.stream(lines = 2).enqueue(callback)
        apiService.streamBytes(bytes = 2).enqueue(callback)
        apiService.image(accept = "image/webp").enqueue(callback)
        apiService.gzip().enqueue(callback)
        apiService.utf8().enqueue(callback)
        apiService.xml().enqueue(callback)
    }

    @Composable
    fun SimpleWebView() {
        AndroidView(factory = { context ->
            webview(context).apply {
                // 加载网页
                loadUrl("https://juejin.cn/")
            }
        }, update = { webView ->
            // 可以在这里处理更新逻辑，例如重新加载页面等
            webView.reload()
        })
    }
    private fun webview(context: Context):WebView {
       val webview = WebView(context)
        webview?.clearHistory()
        webview?.clearCache(true)
        webview?.clearFormData()
        webview?.clearMatches()
        webview?.settings?.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            useWideViewPort = true
            javaScriptCanOpenWindowsAutomatically = true
            loadWithOverviewMode = true
            displayZoomControls = false
            setSupportMultipleWindows(true)
            loadsImagesAutomatically = true
            blockNetworkImage = false
            setGeolocationEnabled(true)
            databaseEnabled = true
            setSupportZoom(false)
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webview?.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                // 1. 创建临时WebView用于捕获URL
                val result = view?.hitTestResult
                val url = result?.extra ?: "" // 获取点击链接的URL
                Log.d("shouldLoading", "_blank: $url")
                if (url.startsWith("http", true)) {
                    view?.loadUrl(url)
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    if (packageManager.resolveActivity(
                            intent,
                            PackageManager.MATCH_DEFAULT_ONLY
                        ) != null
                    ) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, "不支持", Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }
        }
        webview?.webViewClient = object : WebViewClient() {

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.proceed()//忽略证书错误
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.d("shouldLoading", "request: $url")
                if (url?.startsWith("http", true) == true) {
                    view?.loadUrl(url)
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, url?.toUri())
                    if (packageManager.resolveActivity(
                            intent,
                            PackageManager.MATCH_DEFAULT_ONLY
                        ) != null
                    ) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, "不支持", Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return this.shouldOverrideUrlLoading(view, request?.url?.toString())
            }
        }
        this.webview = webview
        return webview
    }

    override fun onDestroy() {
        super.onDestroy()
        webview?.destroy()
    }
}