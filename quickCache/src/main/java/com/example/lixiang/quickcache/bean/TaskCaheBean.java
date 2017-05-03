package com.example.lixiang.quickcache.bean;

import android.content.Context;
import android.os.Handler;

/**
 * Created by lixiang on 2017/5/1.
 */
public class TaskCaheBean {
    public Context context;
    public Handler UIHandler;
    public LoadingCacheStringBean loadingCacheStringBean;

    public Context getContext() {
        return context;
    }

    public TaskCaheBean setContext(Context context) {
        this.context = context;
        return this;
    }

    public Handler getUIHandler() {
        return UIHandler;
    }

    public TaskCaheBean setUIHandler(Handler mUIHandler) {
        this.UIHandler = mUIHandler;
        return this;
    }

    public LoadingCacheStringBean getLoadingCacheStringBean() {
        return loadingCacheStringBean;
    }

    public TaskCaheBean setLoadingCacheStringBean(LoadingCacheStringBean loadingCacheStringBean) {
        this.loadingCacheStringBean = loadingCacheStringBean;
        return this;
    }
}
