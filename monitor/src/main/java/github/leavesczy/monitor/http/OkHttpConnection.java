package github.leavesczy.monitor.http;


import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cookie;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class OkHttpConnection extends HttpURLConnection {
    private final OkHttpClient okHttpClient;
    private Buffer requestBodyBuffer;
    private Response okHttpResponse; // 保存OkHttp响应
    private Proxy mProxy;

    public OkHttpConnection(URL u) {
        super(u);
        // 直接连接，不使用代理
        this.okHttpClient = createOkhttpClient(null);
    }

    public OkHttpConnection(URL u, Proxy p) {
        super(u);
        mProxy = p;
        this.okHttpClient = createOkhttpClient(p);
    }

    @Override
    public void connect() throws IOException {
        // 构建OkHttp请求
        Request request = new Request.Builder()
                .url(url)
                .method(getRequestMethod(), getRequestBody())
                .headers(convertHeaders(getRequestProperties()))
                .build();

        // 执行同步请求（异步需回调处理）
        okHttpResponse = this.okHttpClient.newCall(request).execute();
        connected = true;
        requestBodyBuffer = null;
    }

    private RequestBody getRequestBody() {
        Map<String, List<String>> headers = getRequestProperties();
//        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
//            Log.e("Http Header", entry.getKey() + ": " + entry.getValue());
//        }
        List<String> contentTypeList;
        if (headers.containsKey("Content-Type")) {
            contentTypeList = headers.get("Content-Type");
        } else {
            contentTypeList = List.of("text/plain");
        }
        // TODO 没测试文件上传
        if ("POST".equalsIgnoreCase(getRequestMethod()) || "PUT".equalsIgnoreCase(getRequestMethod())) {
            // POST/PUT 必须提供非空 RequestBody, 否则 OkHttp 会抛出异常。
            if (requestBodyBuffer != null) {
//                ByteString byteString = requestBodyBuffer.snapshot();
//                byte[] byteArray = byteString.toByteArray();
                byte[] byteArray = requestBodyBuffer.readByteArray();
                if (contentTypeList != null) {
                    for (String contentType : contentTypeList) {
                        //RequestBody.create()直接生成请求体，支持 String、byte[]或 File
                        return RequestBody.create(byteArray, MediaType.parse(contentType.split(";")[0]));
                    }
                }
            }
//            String json = "{\"key\":\"value\"}";
//            return RequestBody.create(json, MediaType.parse("application/json"));
//            return new FormBody.Builder()
//                    .add("username", "admin")
//                    .add("password", "123456")
//                    .build();
//            return new MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addFormDataPart("file", file.getName(),
//                            RequestBody.create(file, MediaType.parse("application/octet-stream")))
//                    .build();
        }
        return null; // GET/DELETE 等无请求体的方法返回null即可
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (!connected) connect();
        return okHttpResponse.body() != null ? okHttpResponse.body().byteStream() : null; // 返回响应流
    }

    @Override
    public InputStream getErrorStream() {
        return okHttpResponse.body() != null ? okHttpResponse.body().byteStream() : null; // 返回响应流
    }

    @Override
    public OutputStream getOutputStream() {
        // 用于POST/PUT请求体写入
        requestBodyBuffer = new Buffer();
        return requestBodyBuffer.outputStream();
    }

    // 转换请求头
    private Headers convertHeaders(Map<String, List<String>> properties) {
        Headers.Builder builder = new Headers.Builder();
        for (Map.Entry<String, List<String>> entry : properties.entrySet()) {
            for (String value : entry.getValue()) {
                builder.add(entry.getKey(), value);
            }
        }
        return builder.build();
    }

    // 其他方法需重写（如getResponseCode()、disconnect()等）
    @Override
    public int getResponseCode() {
        return okHttpResponse.code();
    }

    @Override
    public void disconnect() {
        if (okHttpResponse != null) okHttpResponse.close();
        connected = false;
    }

    @Override
    public boolean usingProxy() {
        return mProxy != null;
    }


    @SuppressLint("TrustAllX509TrustManager")
    private OkHttpClient createOkhttpClient(Proxy mProxy) {
        X509TrustManager trustManager = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

        };
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            // trustAllCerts信任所有的证书
            sc.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .sslSocketFactory(sc.getSocketFactory(), trustManager)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            // return HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
                            return true;
                        }
                    })
                    .followRedirects(true)  // 处理重定向默认为 true
                    .followSslRedirects(true);  // 处理重定向默认为 true

            builder.cookieJar(new okhttp3.CookieJar() {
                @NonNull
                @Override
                public List<Cookie> loadForRequest(@NonNull HttpUrl httpUrl) {
                    List<Cookie> cookies = new ArrayList<>();
                    String cookieStr = CookieManager.getInstance().getCookie(httpUrl.toString());
                    if (TextUtils.isEmpty(cookieStr)) {
                        return cookies;
                    }
                    for (String it : cookieStr.split(";")) {
                        cookies.add(Cookie.parse(httpUrl, it.trim()));
                    }
                    return cookies;
                }

                @Override
                public void saveFromResponse(@NonNull HttpUrl httpUrl, @NonNull List<Cookie> cookies) {
                    for (Cookie cookie : cookies) {
                        CookieManager.getInstance()
                                .setCookie(url.toString(), cookie.name() + "=" + cookie.value());
                    }
                    CookieManager.getInstance().flush();
                }

            });
            return builder.build();
        } catch (Exception e) {
            return new OkHttpClient();
        }
    }
}
