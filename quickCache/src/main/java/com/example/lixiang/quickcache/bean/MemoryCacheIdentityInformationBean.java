package com.example.lixiang.quickcache.bean;

/**
 * Created by lixiang on 2017/4/5.
 */
public class MemoryCacheIdentityInformationBean {


    /**
     * @apiNote 每一条CacheItem的IdentityInformation(身份信息)
     * @param value : value
     * @param validTime ：有效时间
     * @param createTime ：创建时间（计时的起点）
     * @param lastTime ：最后访问的时间
     *@author LiXaing
     *create at 2017/4/4 15:01
     */


    public MemoryCacheIdentityInformationBean( String value, int validTime, long createTime, long lastTime, long itemSize) {
        this.value = value;
        this.lastTime = lastTime;
        this.validTime = validTime;
        this.createTime = createTime;
        this.itemSize = itemSize;
    }

    String value;
    long lastTime;
    int validTime;
    long createTime;
    long itemSize;

    public long getItemSize() {
        return itemSize;
    }

    public void setItemSize(long itemSize) {
        this.itemSize = itemSize;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public int getValidTime() {
        return validTime;
    }

    public void setValidTime(int validTime) {
        this.validTime = validTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
