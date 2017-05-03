package com.example.lixiang.quickcache;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.example.lixiang.quickcache.bean.LoadingCacheStringBean;
import com.example.lixiang.quickcache.bean.TaskCaheBean;
import com.example.lixiang.quickcache.exception.DataSpecificationException;
import com.example.lixiang.quickcache.manager.CacheManager;
import com.example.lixiang.quickcache.manager.RequestCacheManager;
import com.example.lixiang.quickcache.okhttp.OkHttpUtils;
import com.example.lixiang.quickcache.taskscheduler.ThreadTheTaskScheduler;
import com.example.lixiang.quickcache.utils.FileUtil;
import com.example.lixiang.quickcache.utils.md5Util;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by lixiang on 2017/3/27.
 */
public class QuickCacheUtil extends ThreadTheTaskScheduler {


    private static QuickCacheUtil quickCacheUtil = new QuickCacheUtil();
    private Context context;
    public static String UserName = "tempFile";

    public static String getCacheDirUserName() {
        return UserName;
    }

    public static void setCacheDirUserName(String userName) {
        UserName = userName;
    }

    private QuickCacheUtil() {}

    public static QuickCacheUtil getInstance() {

        return quickCacheUtil;
    }



    public enum RequestType{
        post,
        put,
        delete,
        get;
    }

    //从APPlication里面获取context
    public  void setContext(Context contexts) {
        context = contexts;
        OkHttpUtils.setContext(contexts);
        OkHttpUtils client = OkHttpUtils.getInstance();
        client.setConnectTimeout(30000, TimeUnit.MILLISECONDS);
    }


//

    /**
     * @apiNote 本类为单利，通过getInstance()获得本类的唯一引用
     * @author LiXaing
     * create at 2017/3/28 18:25
     */
        private  CacheManager chacheManager = new CacheManager();
    public  CacheManager getCacheManager() {
        if (chacheManager.getContext() == null) {
            chacheManager.setContext(context);
        }
        return chacheManager;
    }

    HashSet<Object> tagHashSet= new HashSet<Object>();

    public void cancelTag(Object tag){
        synchronized (this){
        tagHashSet.remove(tag);
//        if (openNetWork) {
            OkHttpUtils.getInstance().cancelTag(tag);
//        }
        }

    }

    public static class UIHandler<T extends UIHandler.BaseHandlerCallBack> extends Handler {

        SoftReference<T> wr;

        public UIHandler(T t) {
            wr = new SoftReference<T>(t);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            System.out.println(" Message："+msg);

            T t = wr.get();
            if (t != null) {
                t.callBack(msg);
            }
        }

        public interface BaseHandlerCallBack {
             void callBack(Message msg);
        }
    }



    /**
    * @apiNote 用于加载缓存数据
     * @return LoadingCacheStringBean: 需要填充的数据对象
    *@author LiXaing
    *create at 2017/5/2 15:16
    */
    public LoadingCacheStringBean LoadingCacheString(){

        return new LoadingCacheStringBean() {
            @Override
            public void commitListener() {
//                cacheCommit(this);
        LoadingCacheString(this);
            }
        };
    }

    private    void LoadingCacheString(LoadingCacheStringBean loadCacheStringBean){

        synchronized (this){
        tagHashSet.add(loadCacheStringBean.getTag());
//                System.out.println("tagHashSet.size():  " + tagHashSet.size());
            if (loadCacheStringBean.getAlias() != null) {
            setAlias(loadCacheStringBean.getRequestType(),loadCacheStringBean.getUrl(),loadCacheStringBean.getParams(),loadCacheStringBean.getAlias());
            }
        }


//			本handler是在主线程中创建的；当获取到子线程返回的holder对象就判断其target是否还相等
            UIHandler mUIHandler = new UIHandler<>(new UIHandler.BaseHandlerCallBack() {
                @Override
                public void callBack(Message msg) {
                    if (tagHashSet.contains(loadCacheStringBean.getTag())) {
                        String cacheDate = msg.getData().getString("CacheDate");
                        if (cacheDate != null) {
                            loadCacheStringBean.getOrc().onResponseCache(cacheDate);
                        }else {
                            loadCacheStringBean.getOrc().onRequestNetWork();
                        }
                    }
                }
            });

        /** 判断当前是否需要刷新缓存数据,当需要刷新缓存数据就不从内存中读取
        * @author LiXaing create at 2017/5/2 22:42 */
        if (!loadCacheStringBean.isRefreshCache()) {

            String type = getRequestTypr(loadCacheStringBean.getRequestType());
            String memoryCache = getCacheManager().get(md5Util.md5(loadCacheStringBean.getUrl()) + File.separator + md5Util.md5(type + loadCacheStringBean.getUrl() + loadCacheStringBean.getParams().hashCode()));
//        防止当展示页面被回收，依旧进行数据返回则会发生内存泄漏
            if (memoryCache != null) {
                if (tagHashSet.contains(loadCacheStringBean.getTag())) {
                    loadCacheStringBean.getOrc().onResponseCache(memoryCache);
                }
                return ;
            }
        }

        addTask(buildTask(new TaskCaheBean()
                .setContext(context)
                .setUIHandler(mUIHandler)
                .setLoadingCacheStringBean(loadCacheStringBean)));
    }

    @NonNull
    public static String getRequestTypr(RequestType requestType) {
        String type = null;
        switch (requestType) {
            case post:
            type = "post";
                break;
            case get:
            type = "get";
                break;
            case put:
            type = "put";
                break;
            case delete:
            type = "delete";
                break;
        }
        return type;
    }



    /**
    * @apiNote 向本地写入Cache
     * @param requestType ：请求类型
     * @param fileName ：url
     * @param m_params ：请求参数
     * @param cache ：缓存数据
     * @param validTime ：有效期时间
     *@author LiXaing
    *create at 2017/5/2 15:20
    */
    public void putCacheString(RequestType requestType, String fileName, Map<String, String> m_params, String cache, int validTime){
        HashMap params ;
        if (m_params != null) {
            params = (HashMap) m_params;
        }else {
            params = new HashMap<String, String> ();
        }

        String type = getRequestTypr(requestType);
        try {

            getCacheManager()
                    .put(md5Util.md5(fileName)+File.separator + md5Util.md5(type + fileName + params.hashCode()),
                            cache,
                            validTime,
                            System.currentTimeMillis(),
                            System.currentTimeMillis());
        } catch (DataSpecificationException e) {
            e.printStackTrace();
        }

        File file = FileUtil.getDiskCacheDir(context,
                 md5Util.md5(fileName)+File.separator + md5Util.md5(requestType + fileName + params.hashCode()));
        getCacheManager()
                .writeLoaclCache(file,
                        cache,
                        validTime,
                        System.currentTimeMillis(),
                        System.currentTimeMillis());

    }




    private   void removeAll(String key) {
        getCacheManager().deleteCacheItemAll(key);
    }


    /**
     * 清空内存缓存
     *@author LiXaing
     *create at 2017/4/26 23:47
     */
    public void cleanMemoryCache() {
        getCacheManager().cleanMemory();
    }




    @Override
    public void runThreadPoolChildThread(TaskCaheBean taskCaheBean) {
        RequestCacheManager.RequestCache(taskCaheBean);
    }

    @Override
    public void CacheContainer() {

    }


    /**
    * @apiNote ：为一接口设置别名
     * @param requestType ：请求类型
     * @param url ：url
     * @param params ：请求参数
     * @param alias ：需要设定的别名
     *@author LiXaing
    *create at 2017/5/2 15:24
    */
    public void setAlias(RequestType requestType,String url,Map<String, String> params, String alias) {
        String value = md5Util.md5(url)+File.separator + md5Util.md5(requestType + url + params.hashCode());
        getCacheManager().putAlias(md5Util.md5(alias),value);
    }



    /**
    * @apiNote : 通过别名删除内存以及本地中的一条Cache
     * @param alias ：别名
     *@author LiXaing
    *create at 2017/5/2 15:25
    */
    public void deleteAliasCache(String alias){
        String value = getCacheManager().getAlias(md5Util.md5(alias));
        if (value ==null) {
//            throw new NullPointerException("There is no this alias");
            return;
        }
        synchronized (this){
        removeAll(value);
        }
    }


    /**
    * @apiNote ：通过别名删除内存以及本地中的一条Cache
     * @param requestType ：请求类型
     * @param fileName : URL
     * @param params : 请求参数
    *@author LiXaing
    *create at 2017/5/2 15:26
    */
    public  void deleteKeyCache(RequestType requestType,String fileName,Map<String, String> params) {
        getCacheManager().deleteCacheItemAll(md5Util.md5(fileName)+File.separator + md5Util.md5(requestType + fileName + params.hashCode()));
    }



//    public void deleteAliasPagingCache(String url,String PagingRules,int index){
//        String dirPath =  FileUtil.getDiskCacheDirPath(context)+File.separator +"QuickCacheFile" + File.separator + md5Util.md5(url);
//        File file = new File(dirPath);
//        long size = FileUtil.getFileList(file);
//            deleteAliasCache(url + PagingRules+index);
//    }


    /**
     * @apiNote : 删除指定url的所有缓存（包括内存中）
    * @param
    *@author LiXaing
    *create at 2017/5/2 15:36
    */
    public void deleteCacheDir(String url){
//        String dirPath = FileUtil.getDiskCacheDirPath(context)+ File.separator +"QuickCacheFile" + File.separator + md5Util.md5(url);
        String dirPath = FileUtil.getDiskCacheDirPath(context)+ File.separator + md5Util.md5(url);
        deleteDirectory(dirPath);
    }

    private   void deleteDirectory(String fileName) {
        if (!fileName.equals("")) {
            File newPath = new File(fileName);
            if (newPath.isDirectory()) {
                String[] listfile = newPath.list();
                try {
                    for (int i = 0; i < listfile.length; i++) {
                        File deletedFile = new File(newPath.toString() + "/"
                                + listfile[i].toString());
                        String key = newPath.getName() + File.separator + deletedFile.getName();
                        removeAll(key);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }
    }
    }



    public void removeAliasa(String alias){
        getCacheManager().removeAlias(md5Util.md5(alias));
    }
}
