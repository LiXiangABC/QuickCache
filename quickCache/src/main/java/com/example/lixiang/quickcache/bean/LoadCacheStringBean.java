package com.example.lixiang.quickcache.bean;

import com.example.lixiang.quickcache.QuickCacheUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lixiang on 2017/4/17.
 */
public class LoadCacheStringBean {
//    t
    private QuickCacheUtil.RequestType requestType;
    private String url;
    private Map<String, String> params;
    private int validTime = 8*60;
    private Object tag;
    private onResponseCacheListener orc;
    private String alias;
    private boolean isRefreshCache = false;

    public boolean isRefreshCache() {
        return isRefreshCache;
    }

    public void setIsRefreshCache(boolean isRefreshCache) {
        this.isRefreshCache = isRefreshCache;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public QuickCacheUtil.RequestType getRequestType() {
        return requestType;
    }

    public LoadCacheStringBean setRequestType(QuickCacheUtil.RequestType requestType) {
        this.requestType = requestType;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public LoadCacheStringBean setUrl(String url) {
        this.url = url;
        return this;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public LoadCacheStringBean setParams(Map<String, String> params) {
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

    public LoadCacheStringBean setValidTime(int validTime) {
        this.validTime = validTime;
        return this;
    }

    public Object getTag() {
        return tag;
    }

    public LoadCacheStringBean setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public onResponseCacheListener getOrc() {
        return orc;
    }

    public LoadCacheStringBean setOrc(onResponseCacheListener orc) {
        this.orc = orc;
        return this;
    }

    public void commit(){
        QuickCacheUtil.getInstance().commit(this);
    }
}
