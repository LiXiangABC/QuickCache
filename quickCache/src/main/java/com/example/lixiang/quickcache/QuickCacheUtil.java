package com.example.lixiang.quickcache;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.example.lixiang.quickcache.bean.LoadCacheStringBean;
import com.example.lixiang.quickcache.exception.DataSpecificationException;
import com.example.lixiang.quickcache.manager.CacheManager;
import com.example.lixiang.quickcache.manager.RequestCacheManager;
import com.example.lixiang.quickcache.okhttp.OkHttpUtils;
import com.example.lixiang.quickcache.taskscheduler.ThreadTheTaskScheduler;
import com.example.lixiang.quickcache.utils.FileUtil;
import com.example.lixiang.quickcache.utils.md5Util;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by lixiang on 2017/3/27.
 */
public class QuickCacheUtil extends ThreadTheTaskScheduler {


    private static QuickCacheUtil quickCacheUtil = new QuickCacheUtil();
    private Context context;
    public static String UserName = "";

    public static String getCacheDirUserName() {
        return UserName;
    }

    public static void setCacheDirUserName(String userName) {
        UserName = userName;
    }

    private QuickCacheUtil() {
//        在创建的时候初始化保证集合的线程安全
//        Collections.synchronizedMap(new ArrayMap<String, SoftReference<String>>());
    }

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

    private  boolean openNetWork = false;

    public void openNetWork(boolean isOpen){
        openNetWork = isOpen;
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

        WeakReference<T> wr;

        public UIHandler(T t) {
            wr = new WeakReference<T>(t);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            T t = wr.get();
            if (t != null) {
                t.callBack(msg);
            }
        }

        public interface BaseHandlerCallBack {
            public void callBack(Message msg);
        }
    }

    public LoadCacheStringBean LoadCacheString(){

        return new LoadCacheStringBean();
    }

    public void commit(LoadCacheStringBean loadCacheStringBean){
        LoadCacheString(loadCacheStringBean);
    }

    private    void LoadCacheString(LoadCacheStringBean loadCacheStringBean){

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
//            {
//                public void handleMessage(Message msg)
//                {


        String type = getRequestTypr(loadCacheStringBean.getRequestType());
//        md5Util.md5(fileName)+File.separator + md5Util.md5(requestType + fileName + params.hashCode()
        String memoryCache = getCacheManager().get(md5Util.md5(loadCacheStringBean.getUrl()) + File.separator + md5Util.md5(type + loadCacheStringBean.getUrl() + loadCacheStringBean.getParams().hashCode()));


//        防止当展示页面被回收，依旧进行数据返回则会发生内存泄漏
        if (memoryCache != null) {
            if (tagHashSet.contains(loadCacheStringBean.getTag())) {
                loadCacheStringBean.getOrc().onResponseCache(memoryCache);
            }
//            else {
//                loadCacheStringBean.getOrc().onRequestNetWork();
//            }
        return ;
        }

        addTask(buildTask(context, loadCacheStringBean.getUrl(), loadCacheStringBean.getParams(), loadCacheStringBean.getValidTime(), loadCacheStringBean.isRefreshCache(), type, loadCacheStringBean.getTag(),mUIHandler));
    }

    @NonNull
    private String getRequestTypr(RequestType requestType) {
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
                "QuickCacheFile" + File.separator +md5Util.md5(fileName)+File.separator + md5Util.md5(requestType + fileName + params.hashCode()));
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
    public void runThreadPoolChildThread(Context context,String fileName,Map<String, String> params,int validTime,
                                         boolean openNetWork,String requestType,Object tag,Handler mUIHandler) {
        RequestCacheManager.RequestCache(context, fileName, params, validTime, openNetWork, requestType, tag,mUIHandler);
    }

    @Override
    public void CacheContainer() {

    }

    public void setAlias(RequestType requestType,String url,Map<String, String> params, String alias) {
        String value = md5Util.md5(url)+File.separator + md5Util.md5(requestType + url + params.hashCode());
        getCacheManager().putAlias(md5Util.md5(alias),value);
    }

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


    public  void deleteKeyCache(RequestType requestType,String fileName,Map<String, String> params) {
        getCacheManager().deleteCacheItemAll(md5Util.md5(fileName)+File.separator + md5Util.md5(requestType + fileName + params.hashCode()));
    }



    public void deleteAliasPagingCache(String url,String PagingRules,int index){

        String dirPath =  FileUtil.getDiskCacheDirPath(context)+File.separator +"QuickCacheFile" + File.separator + md5Util.md5(url);


        File file = new File(dirPath);
//        File file = FileUtil.getDiskCacheDir(context,
//                "QuickCacheFile" + File.separator + md5Util.md5(url));
        long size = FileUtil.getFileList(file);

//        for (long i = 0; i < size; i++) {
//            File file1 = new File(dirPath+ File.separator +PagingRules+(i+1));
//            file1.delete();
//        System.out.println("deleteAliasCache:"+md5Util.md5(url+PagingRules+(i+1))+"     long size :"+size);
            deleteAliasCache(url + PagingRules+index);
//        }
    }


    public void deleteCacheDir(String url){
        String dirPath = FileUtil.getDiskCacheDirPath(context)+ File.separator +"QuickCacheFile" + File.separator + md5Util.md5(url);
        deleteDirectory(dirPath);
    }

    private   void deleteDirectory(String fileName) {
//        SecurityManager checker = new SecurityManager();

        if (!fileName.equals("")) {

//			File path = Environment.getExternalStorageDirectory();
//			path.toString() +
            File newPath = new File(fileName);
//            checker.checkDelete(newPath.toString());
            if (newPath.isDirectory()) {
                String[] listfile = newPath.list();
                try {
                    for (int i = 0; i < listfile.length; i++) {
                        File deletedFile = new File(newPath.toString() + "/"
                                + listfile[i].toString());
//                        deletedFile.delete();
                        String key = newPath.getName() + File.separator + deletedFile.getName();
                        removeAll(key);
                    }
//					newPath.delete();
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
