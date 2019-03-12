/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.toolbox;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.wcb.OkHttpClientStatck;

import java.io.File;
/**
 * 用途：
 *  初始化Volley中网络配置，异步线程配置，磁盘缓存配置
 */
public class Volley {

    /** Default on-disk cache directory. */
    /** Default on-disk cache directory.   默认缓存的文件夹名*/
    private static final String DEFAULT_CACHE_DIR = "volley";

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @param stack A {@link BaseHttpStack} to use for the network, or null for default.
     * @return A started {@link RequestQueue} instance.
     * * 创建一个默认的工作池对象，且调用RequestQueue的start()
     *      *  参数Contentxt用于创建磁盘缓存的文件夹
     *      *  参数HttpStack用于网络工作，默认是nulll
     */
    public static RequestQueue newRequestQueue(Context context, BaseHttpStack stack) {
        BasicNetwork network;
        if (stack == null) {
            //api版本不小于9，则使用java中HttpURLConnection作为联网方式
            if (Build.VERSION.SDK_INT >= 9) {
                //创建一个执行网络工作的操作类
                network = new BasicNetwork(new HurlStack());
            } else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                // At some point in the future we'll move our minSdkVersion past Froyo and can
                // delete this fallback (along with all Apache HTTP code).
                String userAgent = "volley/0";
                try {
                    String packageName = context.getPackageName();
                    PackageInfo info =
                            context.getPackageManager().getPackageInfo(packageName, /* flags= */ 0);
                    userAgent = packageName + "/" + info.versionCode;
                } catch (NameNotFoundException e) {
                }

                network =
                        new BasicNetwork(
                                new HttpClientStack(AndroidHttpClient.newInstance(userAgent)));
            }
        } else {
            network = new BasicNetwork(stack);
        }

        return newRequestQueue(context, new OkHttpClientStatck());
    }

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @param stack An {@link HttpStack} to use for the network, or null for default.
     * @return A started {@link RequestQueue} instance.
     * @deprecated Use {@link #newRequestQueue(Context, BaseHttpStack)} instead to avoid depending
     *     on Apache HTTP. This method may be removed in a future release of Volley.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static RequestQueue newRequestQueue(Context context, HttpStack stack) {
        if (stack == null) {
            return newRequestQueue(context, (BaseHttpStack) null);
        }
        return newRequestQueue(context, new BasicNetwork(stack));
    }

    private static RequestQueue newRequestQueue(Context context, Network network) {
        //在手机内存中创建一个缓存数据的文件夹
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
        //创建一个请求队列，添加磁盘缓存的操作类，执行网络工作的操作类
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        //开启。
        queue.start();
        return queue;
    }

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @return A started {@link RequestQueue} instance.
     *     创建一个默认的工作池对象，且调用RequestQueue的start()
     *     参数Contentxt用于创建磁盘缓存的文件夹
     */
    public static RequestQueue newRequestQueue(Context context) {
        return newRequestQueue(context, (BaseHttpStack) null);
    }
}
