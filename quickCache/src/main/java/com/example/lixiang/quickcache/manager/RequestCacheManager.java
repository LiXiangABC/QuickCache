package com.example.lixiang.quickcache.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.lixiang.quickcache.QuickCacheUtil;
import com.example.lixiang.quickcache.exception.DataSpecificationException;
import com.example.lixiang.quickcache.okhttp.OkHttpUtils;
import com.example.lixiang.quickcache.okhttp.callback.Callback;
import com.example.lixiang.quickcache.utils.FileUtil;
import com.example.lixiang.quickcache.utils.md5Util;

import java.io.File;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by lixiang on 2017/3/27.
 */
public class RequestCacheManager {

    public static void RequestCache(Context context,String fileName,Map<String, String> params,int validTime,
                                      boolean openNetWork,String requestType,Object tag,Handler mUIHandler){
    File file = FileUtil.getDiskCacheDir(context,
            md5Util.md5(fileName)+File.separator + md5Util.md5(requestType + fileName + params.hashCode()));
//            "QuickCacheFile" + File.separator + fileName);

        String mCache = null;
        Bundle localCache = null;
        if (file != null) {

            localCache = getLocalCache(file);
        }
        if (localCache != null) {
                mCache =  localCache.getString("cache");
        }
        if (!openNetWork) {
            if (localCache == null || mCache == null ) {
            sendCacheMessage(mUIHandler, null);
                return;
            }
        }

        if (mCache != null) {
                                sendCacheMessage(mUIHandler, mCache);
            try {

                QuickCacheUtil.getInstance().getCacheManager()
                        .put(md5Util.md5(fileName)+File.separator + md5Util.md5(requestType + fileName + params.hashCode()),
                                mCache,
                                Integer.parseInt(localCache.getString("validTime")),
                                Long.parseLong(localCache.getString("createTime")),
                                System.currentTimeMillis());
            } catch (DataSpecificationException e) {
                e.printStackTrace();
            }

        }else {
//            当不为空的时候就进行网络的访问
            if (openNetWork) {
                getNetWorkCache(requestType, fileName, file, params, tag, validTime, mUIHandler);
            }else {
            sendCacheMessage(mUIHandler, null);
            }

        }
    }

    private static void sendCacheMessage(Handler mUIHandler, String mCache) {
        Bundle bundle = new Bundle();
        bundle.putString("CacheDate", mCache);
        Message message = Message.obtain();
        message.setData(bundle);
        mUIHandler.sendMessage(message);
    }

    private static String getNetWorkCache(String requestType, String fileName, File file, Map<String, String> params, Object tag, int validTime, Handler mUIHandler) {
        switch (requestType) {
            case "post":
                 postRequest(fileName,file,params,tag,validTime,mUIHandler);
                break;

        case "get":
            getRequest(fileName,file,params,tag,validTime,mUIHandler);
                break;
        }
        return null;
    }


    private static void getRequest(String fileName, File file, Map<String, String> params, Object tag, int validTime, Handler mUIHandler) {
        OkHttpUtils.get()
                .tag(tag)
                .params(params)
                .url(fileName).build().execute(new Callback() {
            @Override
            public Object parseNetworkResponse(Response response)
                    throws Exception {
                return response.body().string();
            }

            @Override
            public void onError(Call call, Exception e) {
            }

            @Override
            public void onResponse(Object response) {
                if (response != null) {
                    sendCacheMessage(mUIHandler, response.toString());

                    try {
                        QuickCacheUtil.getInstance().getCacheManager()
                                .put(md5Util.md5(fileName)+File.separator + md5Util.md5("get" + fileName + params.hashCode()),
                                        response.toString(),
                                        validTime,
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis());
                    } catch (DataSpecificationException e) {
                        e.printStackTrace();
                    }
                    new Thread(()->{
                        QuickCacheUtil.getInstance().getCacheManager()
                                .writeLoaclCache(file,
                                        response.toString(),
                                        validTime,
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis());

                    }).start();

                }else {
                    sendCacheMessage(mUIHandler, null);
                }
            }

        });
    }

    private static void postRequest(String fileName, File file, Map<String, String> params, Object tag, int validTime, Handler mUIHandler) {
        OkHttpUtils.post()
                .tag(tag)
                .params(params)
                .url(fileName).build().execute(new Callback() {
            @Override
            public Object parseNetworkResponse(Response response)
                    throws Exception {
                return response.body().string();
            }

            @Override
            public void onError(okhttp3.Call call, Exception e) {

            }


            @Override
            public void inProgress(float progress) {
                super.inProgress(progress);

            }


            @Override
            public void onResponse(Object response) {
                if (response != null) {
                    System.out.println("CacheResponse.toString()" + response.toString());
                    sendCacheMessage(mUIHandler, response.toString());

                    try {
                        QuickCacheUtil.getInstance().getCacheManager()
                                .put(md5Util.md5(fileName)+File.separator + md5Util.md5("post" + fileName + params.hashCode()),
                                        response.toString(),
                                        validTime,
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis());
                    } catch (DataSpecificationException e) {
                        e.printStackTrace();
                    }


                    new Thread(()->{
                        QuickCacheUtil.getInstance().getCacheManager()
                                .writeLoaclCache(file,
                                        response.toString(),
                                        validTime,
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis());

                    }).start();

                }else {
                    sendCacheMessage(mUIHandler, null);
                }
            }

        });
    }

    private static Bundle getLocalCache(File file) {
                Bundle bundle = QuickCacheUtil.getInstance().getCacheManager().readLoaclCache(file);
        return  bundle;

    }
}
