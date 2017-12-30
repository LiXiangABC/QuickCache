package com.example.lixiang.quickcache.manager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.lixiang.quickcache.QuickCacheUtil;
import com.example.lixiang.quickcache.bean.TaskCaheBean;
import com.example.lixiang.quickcache.exception.DataSpecificationException;
import com.example.lixiang.quickcache.utils.FileUtil;
import com.example.lixiang.quickcache.utils.md5Util;

import java.io.File;

/**
 * Created by lixiang on 2017/3/27.
 */
public class RequestCacheManager {

    public static void RequestCache(TaskCaheBean taskCaheBean){

        File file = FileUtil.getDiskCacheDir(taskCaheBean.getContext(),
                md5Util.md5(taskCaheBean.getLoadingCacheStringBean().getUrl())
                        +File.separator +
                        md5Util.md5(QuickCacheUtil.getRequestTypr(taskCaheBean.getLoadingCacheStringBean().getRequestType()) +
                                taskCaheBean.getLoadingCacheStringBean().getUrl()
                                + taskCaheBean.getLoadingCacheStringBean().getParams().hashCode()));



        String mCache = null;
        Bundle localCache = null;

        /**        Explain : 判断当前缓存文件是否存在
        * @author LiXiang create at 2017/12/14 22:58*/
        if (file != null) {
            localCache = getLocalCache(file);
        }

        /**        Explain : 在本地缓存文件存在的情况下，判断是否有存在的缓存
        * @author LiXiang create at 2017/12/14 22:59*/
        if (localCache != null) {
                mCache =  localCache.getString("cache");
        }


        /**        Explain : 当没有打开网络访问的时候就直接进行返回空
        * @author LiXiang create at 2017/12/14 23:00*/
        if (!taskCaheBean.getLoadingCacheStringBean().isOpenNetWork()) {
            if (mCache == null ) {
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
            sendCacheMessage(taskCaheBean.getUIHandler(), null);
        }
    }

    public static void sendCacheMessage(Handler mUIHandler, String mCache) {
        Bundle bundle = new Bundle();
        bundle.putString("CacheDate", mCache);
        Message message = Message.obtain();
        message.setData(bundle);
        mUIHandler.sendMessage(message);
    }

    private static Bundle getLocalCache(File file) {
                Bundle bundle = QuickCacheUtil.getInstance().getCacheManager().readLoaclCache(file);
        return  bundle;

    }
}
