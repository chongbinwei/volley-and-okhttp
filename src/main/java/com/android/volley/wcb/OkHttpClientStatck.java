package com.android.volley.wcb;

import com.android.volley.toolbox.HurlStack;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.internal.huc.OkHttpURLConnection;
import okhttp3.internal.huc.OkHttpsURLConnection;

public class OkHttpClientStatck  extends HurlStack {
    private OkHttpClient okHttpClient;

    /**
     * 采购构建者方式创建OkHttpClient
     * OkHttpClient可以自定义拦截器，缓存，联网时间和写入时间等设置。
     * Volley默认有这些东西，这里就不在设置。
     */
    public OkHttpClientStatck(){
        OkHttpClient.Builder builder=new OkHttpClient.Builder();
        okHttpClient=builder.build();
    }
    /**
     * 获取到OkHttpClient();
     * @return
     */
    private  OkHttpClient getOkHttpClient(){
        return  okHttpClient;
    }
    /**
     * 这里采用OkHttp框架中HttpURLConnection，而不使用原生的。
     *
     * OkHttpClient1.x:通过OkHttpClient.open(url)来获取
     *
     * OkHttpClient2.x:可以通过OkUrlFactory.open(URL url)来获取
     *
     * @param url
     * @return
     * @throws IOException
     */
    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {
        String protocol=  url.getProtocol();
        if (protocol.equals("http")) return new OkHttpURLConnection(url, getOkHttpClient(),null);

        if (protocol.equals("https")) return new OkHttpsURLConnection(url, getOkHttpClient(),null);
        throw new IllegalArgumentException("Unexpected protocol: " + protocol);
    }
}
