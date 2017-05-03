package com.example.lixiang.quickcache.manager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.lixiang.quickcache.QuickCacheUtil;
import com.example.lixiang.quickcache.bean.TaskCaheBean;
import com.example.lixiang.quickcache.exception.DataSpecificationException;
import com.example.lixiang.quickcache.okhttp.OkHttpUtils;
import com.example.lixiang.quickcache.okhttp.callback.Callback;
import com.example.lixiang.quickcache.utils.FileUtil;
import com.example.lixiang.quickcache.utils.md5Util;

import java.io.File;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by lixiang on 2017/3/27.
 */
public class RequestCacheManager {

    public static void RequestCache(TaskCaheBean taskCaheBean){
    File file = FileUtil.getDiskCacheDir(taskCaheBean.getContext(),
            md5Util.md5(taskCaheBean.getLoadingCacheStringBean().getUrl())
                    +File.separator +
                    md5Util.md5(QuickCacheUtil.getRequestTypr(taskCaheBean.getLoadingCacheStringBean().getRequestType()) +
                            taskCaheBean.getLoadingCacheStringBean().getUrl() + taskCaheBean.getLoadingCacheStringBean().getParams().hashCode()));
//            "QuickCacheFile" + File.separator + fileName);

        /** 判断当前是否需要刷新缓存数据
         * @author LiXaing create at 2017/5/2 22:42 */
        if (taskCaheBean.getLoadingCacheStringBean().isRefreshCache()) {
            getNetWorkCache(taskCaheBean, file);
            return;
        }


        String mCache = null;
        Bundle localCache = null;
        if (file != null) {
            localCache = getLocalCache(file);
        }
        if (localCache != null) {
                mCache =  localCache.getString("cache");
        }

        if (!taskCaheBean.getLoadingCacheStringBean().isOpenNetWork()) {
            if (localCache == null || mCache == null ) {
            sendCacheMessage(taskCaheBean.getUIHandler(), null);
                return;
            }
        }

        if (mCache != null) {
                                sendCacheMessage(taskCaheBean.getUIHandler(), mCache);
            try {
                QuickCacheUtil.getInstance().getCacheManager()
                        .put(md5Util.md5(taskCaheBean.getLoadingCacheStringBean().getUrl())+
                                        File.separator +
                                        md5Util.md5(taskCaheBean.getLoadingCacheStringBean().getUrl() + taskCaheBean.getLoadingCacheStringBean().getParams().hashCode()),
                                mCache,
                                Integer.parseInt(localCache.getString("validTime")),
                                Long.parseLong(localCache.getString("createTime")),
                                System.currentTimeMillis());
            } catch (DataSpecificationException e) {
                e.printStackTrace();
            }

        }else {
//            当不为空的时候就进行网络的访问
            if (taskCaheBean.getLoadingCacheStringBean().isOpenNetWork()) {
                getNetWorkCache(taskCaheBean, file);
            }else {
            sendCacheMessage(taskCaheBean.getUIHandler(), null);
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

    public static String getNetWorkCache(TaskCaheBean taskCaheBean, File file) {
        switch (QuickCacheUtil.getRequestTypr(taskCaheBean.getLoadingCacheStringBean().getRequestType())) {
            case "post":
                postRequest(taskCaheBean,file);
                break;

        case "get":
            getRequest(taskCaheBean,file);
                break;
        }
        return null;
    }


    private static void getRequest(TaskCaheBean taskCaheBean, File file) {
        OkHttpUtils.get()
                .tag(taskCaheBean.getLoadingCacheStringBean().getTag())
                .params(taskCaheBean.getLoadingCacheStringBean().getParams())
                .url(taskCaheBean.getLoadingCacheStringBean().getUrl()).build().execute(new Callback() {
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
                    System.out.println("get ->CacheResponse.toString()" + response.toString());
                    sendCacheMessage(taskCaheBean.getUIHandler(), response.toString());

                    try {
                        QuickCacheUtil.getInstance().getCacheManager()
                                .put(md5Util.md5(taskCaheBean.getLoadingCacheStringBean().getUrl()) +
                                                File.separator +
                                                md5Util.md5(taskCaheBean.getLoadingCacheStringBean().getUrl() + taskCaheBean.getLoadingCacheStringBean().getParams().hashCode()),
                                        response.toString(),
                                        taskCaheBean.getLoadingCacheStringBean().getValidTime(),
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis());
                    } catch (DataSpecificationException e) {
                        e.printStackTrace();
                    }
                    new Thread(()->{
                        QuickCacheUtil.getInstance().getCacheManager()
                                .writeLoaclCache(file,
                                        response.toString(),
                                        taskCaheBean.getLoadingCacheStringBean().getValidTime(),
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis());

                    }).start();

                }else {
                    sendCacheMessage(taskCaheBean.getUIHandler(), null);
                }
            }

        });
    }

    private static void postRequest(TaskCaheBean taskCaheBean, File file) {

        OkHttpUtils.post()
                .tag(taskCaheBean.getLoadingCacheStringBean().getTag())
                .params(taskCaheBean.getLoadingCacheStringBean().getParams())
                .url(taskCaheBean.getLoadingCacheStringBean().getUrl()).build().execute(new Callback() {
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
                    System.out.println("post ->CacheResponse.toString()" + response.toString());
                    sendCacheMessage(taskCaheBean.getUIHandler(), response.toString());

                    try {
                        QuickCacheUtil.getInstance().getCacheManager()
                                .put(md5Util.md5(taskCaheBean.getLoadingCacheStringBean().getUrl())+
                                                File.separator +
                                                md5Util.md5(taskCaheBean.getLoadingCacheStringBean().getUrl() + taskCaheBean.getLoadingCacheStringBean().getParams().hashCode()),
                                        response.toString(),
                                        taskCaheBean.getLoadingCacheStringBean().getValidTime(),
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis());
                    } catch (DataSpecificationException e) {
                        e.printStackTrace();
                    }


                    new Thread(()->{
                        QuickCacheUtil.getInstance().getCacheManager()
                                .writeLoaclCache(file,
                                        response.toString(),
                                        taskCaheBean.getLoadingCacheStringBean().getValidTime(),
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis());

                    }).start();

                }else {
                    sendCacheMessage(taskCaheBean.getUIHandler(), null);
                }
            }

        });
    }

    private static Bundle getLocalCache(File file) {
                Bundle bundle = QuickCacheUtil.getInstance().getCacheManager().readLoaclCache(file);
        return  bundle;

    }
}
