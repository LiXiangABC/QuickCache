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
import com.example.lixiang.quickcache.taskscheduler.ThreadTheTaskScheduler;
import com.example.lixiang.quickcache.utils.FileUtil;
import com.example.lixiang.quickcache.utils.LogSwitchUtils;
import com.example.lixiang.quickcache.utils.md5Util;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by lixiang on 2017/3/27.
 */
public class QuickCacheUtil extends ThreadTheTaskScheduler<TaskCaheBean> {


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
    }

    public  void  isCheckTimestamp( boolean isCheck){
        chacheManager.setCheckTimestamp(isCheck);
    }
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
//        }
        }

    }

    public static class UIHandler<T extends UIHandler.BaseHandlerCallBack> extends Handler {

//        SoftReference<T> wr;
          T t;
        public UIHandler(T t) {
//            wr = new SoftReference<T>(t);
            this.t = t;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            System.out.println(" Message："+msg);
//            T t = wr.get();
            if (t != null) {
                t.callBack(msg);
            }else {
                LogSwitchUtils.Log("QuickCache UIHandler","SoftReference 为 null");
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
            if (loadCacheStringBean.getAlias() != null) {
            setAlias(loadCacheStringBean.getRequestType(),loadCacheStringBean.getUrl(),loadCacheStringBean.getParams(),loadCacheStringBean.getAlias());
            }
        }


//			本handler是在主线程中创建的；当获取到子线程返回的holder对象就判断其target是否还相等
            UIHandler mUIHandler = new UIHandler<>(new UIHandler.BaseHandlerCallBack() {
                @Override
                public void callBack(Message msg) {
                    LogSwitchUtils.Log("QuickCache UIHandler","收到处理结果信息");
                    if (tagHashSet.contains(loadCacheStringBean.getTag())) {
                    LogSwitchUtils.Log("QuickCache UIHandler","收到处理结果信息，当前界面还处于打开状态");
                        String cacheDate = msg.getData().getString("CacheDate");
                        if (cacheDate != null) {
                         try {
                            loadCacheStringBean.getOrc().onResponseCache(cacheDate);
                            }catch (Exception e){
//                                可能存储的缓存有问题，如果不清理将会导致重复崩溃
                            deleteKeyCache(loadCacheStringBean.getRequestType(),loadCacheStringBean.getUrl() ,loadCacheStringBean.getParams());
                    LogSwitchUtils.Log("QuickCache UIHandler","读取的本地缓存，发生异常");
                            }
                        }else {
                            if (loadCacheStringBean.isOpenNetWork()) {
                            loadCacheStringBean.getOrc().onRequestNetWork();
                            }else {
                                /**        Explain : 设置当前不允许网络获取数据
                                * @author LiXiang create at 2017/12/14 23:02*/
                                LogSwitchUtils.Log("QuickCache UIHandler","设置当前不允许网络获取数据");
                            }
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
            if (memoryCache != null ) {
                if (tagHashSet.contains(loadCacheStringBean.getTag())) {
//                    loadCacheStringBean.getOrc().onResponseCache(memoryCache);
                RequestCacheManager.sendCacheMessage(mUIHandler,memoryCache);
                }
            }else {
                addTask(buildTask(new TaskCaheBean()
                        .setContext(context)
                        .setUIHandler(mUIHandler)
                        .setLoadingCacheStringBean(loadCacheStringBean)));
            }
        }else {
            /**        Explain : 当判断到需要刷新缓存的时候，先将本地的缓存进行删除，这样一来不管网络如何，
             *                   在下一次进来的都会是最新的数据
             *                   注：如果不删除的话，在网络不好或其他非正常情况下，当次更新失败，则后面会一直访问旧的缓存
             * @author LiXiang create at 2017/12/13 16:35*/
            getCacheManager().deleteCacheItemAll(md5Util.md5(loadCacheStringBean.getUrl()) + File.separator + md5Util.md5(getRequestTypr(loadCacheStringBean.getRequestType()) + loadCacheStringBean.getUrl() + loadCacheStringBean.getParams().hashCode()));
            RequestCacheManager.sendCacheMessage(mUIHandler,null);
        }

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

    @NonNull
    public static  RequestType  stringToRequestType(String requestType) {
        RequestType type = null;
        switch (requestType) {
            case "post" :
                type = RequestType.post;
                break;
            case "get":
                type = RequestType.get;
                break;
            case "put":
                type = RequestType.put;
                break;
            case "delete":
                type = RequestType.delete;
                break;
        }
        return type;
    }

    
    /**        Explain : 向本地写入Cache
    * @author LiXaing create at 2017/7/21 11:02*/
    public  void putCacheString(LoadingCacheStringBean LoadingCacheStringBean,String putCacheString){
        putCacheString(LoadingCacheStringBean.getRequestType(),LoadingCacheStringBean.getUrl(),LoadingCacheStringBean.getParams(),
                putCacheString,LoadingCacheStringBean.getValidTime());
    }
    

    /**
    * @apiNote 向本地写入Cache
     * @param requestType ：请求类型
     * @param fileName ：url
     * @param m_params ：请求参数
     * @param cache ：缓存数据
     * 默认有效期时间为8个小时
     *@author LiXaing
    *create at 2017/5/2 15:20
    */
    public synchronized void putCacheString(RequestType requestType, String fileName, Map<String, String> m_params, String cache){
        putCacheString(requestType,fileName,m_params,cache,8*60);
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
    public synchronized void putCacheString(RequestType requestType, String fileName, Map<String, String> m_params, String cache, int validTime){
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
    * @apiNote ：为某接口设置别名，在获取当前接口的缓存时，可通过当前设定的别名来查找
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
     * synchronized:是为了防止，当调用该方法的时候，紧接着就去刷新，并发访问，oldCache还没有dellte
    *create at 2017/5/2 15:25
    */
    public synchronized void deleteAliasCache(String alias){
        String value = getCacheManager().getAlias(md5Util.md5(alias));
        if (value ==null) {
            throw new RuntimeException("There is no this alias");
        }
        removeAll(value);
    }




    /**
    * @apiNote ：通过key删除内存以及本地中的一条Cache
     * @param requestType ：请求类型
     * @param fileName : URL
     * @param params : 请求参数
    *@author LiXaing
    *create at 2017/5/2 15:26
    */
    public  synchronized void deleteKeyCache(RequestType requestType,String fileName,Map<String, String> params) {
        getCacheManager().deleteCacheItemAll(md5Util.md5(fileName)+File.separator + md5Util.md5(requestType + fileName + params.hashCode()));
    }


    /**        Explain : 删除本地的一条指定的缓存
     * @param key ：指定的文件名
     * @author LiXiang create at 2017/12/12 16:44*/
    public synchronized void deleteCacheItemLocal(String key){
        getCacheManager().deleteCacheItemLocal(key);
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
    public synchronized void deleteCacheDir(String url){
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


    /**        Explain : 删除内存中的一条缓存
    * @author LiXiang create at 2017/11/24 18:13*/
    public void  removeMemoryCache(LoadingCacheStringBean LoadingCacheStringBean){
        getCacheManager()
                .remove(md5Util.md5(LoadingCacheStringBean.getUrl())+File.separator + md5Util.md5(getRequestTypr(LoadingCacheStringBean.getRequestType()) + LoadingCacheStringBean.getUrl() + LoadingCacheStringBean.getParams().hashCode()));

    }
    
    /**        Explain : 解除指定别名与任意缓存绑定的关系
    * @author LiXiang create at 2017/11/9 15:39*/
    public void removeAliasa(String alias){
        getCacheManager().removeAlias(md5Util.md5(alias));
    }

    /**
     * 清空指定目录名称下的缓存目录
     *@author LiXaing
     *create at 2017/4/27 0:03
     */
    public void cleanLocalFromUserName(String UserName){
        FileUtil.deleteDirectory(new File(FileUtil.getDiskCacheDirPath(context)).getParentFile().getAbsolutePath() + File.separator + UserName);
    }

    /**        Explain : 清空当前账户下所有的缓存信息
     * @author LiXiang create at 2017/11/9 15:44*/
    public void cleanLocalFromMyself(){
        FileUtil.deleteDirectory(new File(FileUtil.getDiskCacheDirPath(context)).getAbsolutePath());
    }


    /**
     * 清空硬盘中所有的缓存
     *@author LiXaing
     *create at 2017/4/27 0:09
     */
    public void cleanLocalAll(){
        getCacheManager().cleanLocalAll();
    }

    /**
     * 清空内存，硬盘中所有的缓存
     *@author LiXaing
     *create at 2017/4/27 0:11
     */
    public void cleanAll(){
        getCacheManager().cleanAll();
    }
}
