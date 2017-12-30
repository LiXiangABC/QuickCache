package com.example.lixiang.quickcache.bean;

import com.example.lixiang.quickcache.QuickCacheUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lixiang on 2017/4/17.
 */
public abstract class LoadingCacheStringBean {
    private QuickCacheUtil.RequestType requestType;
    private String url = "";
    private Map<String, String> params;
    private int validTime = 8*60;
    private Object tag;
    private onResponseCacheListener orc;
    private String alias = null;
    private boolean isRefreshCache = false;
    private boolean isOpenNetWork = true;

    public boolean isOpenNetWork() {
        return isOpenNetWork;
    }

    public LoadingCacheStringBean setIsOpenNetWork(boolean isOpenNetWork) {
        this.isOpenNetWork = isOpenNetWork;
        return this;
    }

    public boolean isRefreshCache() {
        return isRefreshCache;
    }

    public LoadingCacheStringBean setIsRefreshCache(boolean isRefreshCache) {
        this.isRefreshCache = isRefreshCache;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public LoadingCacheStringBean setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public QuickCacheUtil.RequestType getRequestType() {
        return requestType;
    }

    public LoadingCacheStringBean setRequestType(QuickCacheUtil.RequestType requestType) {
        if (requestType == null) {
            throw new NullPointerException("requestType = null");
        }
        this.requestType = requestType;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public LoadingCacheStringBean setUrl(String url) {
        if (url == null) {
            throw new NullPointerException("url = null");
        }
        this.url = url;
        return this;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public LoadingCacheStringBean setParams(Map<String, String> params) {
        if (params != null) {
        this.params = params;
        }else {
        this.params = new HashMap<String, String>();
        }
        return this;
    }

    public int getValidTime() {
        return validTime;
    }

    public LoadingCacheStringBean setValidTime(int validTime) {
        this.validTime = validTime;
        return this;
    }

    public Object getTag() {
        return tag;
    }

    public LoadingCacheStringBean setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public onResponseCacheListener getOrc() {
        return orc;
    }

    public LoadingCacheStringBean setOrc(onResponseCacheListener orc) {
        this.orc = orc;
        return this;
    }

    public void commit(){
//        QuickCacheUtil.getInstance().commit(this);
        commitListener();
    }

    public abstract void commitListener();
}
