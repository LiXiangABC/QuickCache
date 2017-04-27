package com.example.lixiang.quickcache.manager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;

import com.example.lixiang.quickcache.bean.MemoryCacheIdentityInformationBean;
import com.example.lixiang.quickcache.exception.CacheMapSizeException;
import com.example.lixiang.quickcache.exception.DataSpecificationException;
import com.example.lixiang.quickcache.exception.LoadFactorException;
import com.example.lixiang.quickcache.exception.LocalSizeException;
import com.example.lixiang.quickcache.utils.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by lixiang on 2017/3/27.
 * @apiNote 本类为内存缓存数据的管理器，用于管理对缓存数据的各类操作
 */
public class CacheManager {

    private  Context context;

    public CacheManager() {}


    //从APPlication里面获取context
    public  void setContext(Context contexts) {
        context = contexts;
    }

    public  Context getContext() {
        return context ;
    }


    /**
    * @apiNote 当前缓存的状态码
    *@author LiXaing
    *create at 2017/4/4 15:19
    */
    private String NOT_FIND_FILE = "404";//未发现缓存
    private String SUCCESS = "200";//成功
    private String  EXPIRE = "403.17";//到期




//    private int cacheMapLength = 1000;
    private int cacheMapLength = 10;

    private long cacheMapSize = Runtime.getRuntime().maxMemory()/16;
//    private long cacheMapSize = 3000;

    private long nowCacheMapSize = 0;




    private int localSize = 10;//默认本地缓存区10M

    private float loadFactor = (float) 0.7;

    public CacheManager setLoadFactor(float size) {
        try {
            if (checkLoadFactor(size).equals("pass")) {
                loadFactor = size;
            }
        } catch (LoadFactorException e) {
            System.out.println("LoadFactorException:  " + e);
        }

        return this;
    }



    private String checkLoadFactor(float size) throws LoadFactorException {
        {
            if (size >= 0 && size <= 1) {
                return "pass";
            } else {
                throw new LoadFactorException("The size can not be less than zero, and is greater than one");
            }
        }
    }

    public CacheManager setlocalSize(int size) throws LocalSizeException {
        if (size <= 0) {
            throw new LocalSizeException("The size can not be less than zero");
        }
        localSize = size;
        return this;
    }
    public synchronized  void chechLocalSize(){

        File cacheFile = new File(FileUtil.getDiskCacheDirPath(context) );

//        获取本地文件大小

        long dirSize = FileUtil.getDirSize(cacheFile);
        long mlocalSize = localSize*1024*1024;
        long differSize = dirSize - mlocalSize ;
//        判断是否大于设定值
        if (differSize >0) {

//        定义list，用于存储数据文件的全路径
            List<String> cacheFilelist = new ArrayList<String>();

//        当首选获取本地文件
            List<String> files = FileUtil.getFiles(cacheFile, cacheFilelist);
            int fileIndex = 0;
//        遍历本地文件

            while (differSize >0){
                File file = new File(files.get(fileIndex));
                differSize = differSize - file.length();
                file.delete();
                fileIndex = fileIndex+1;
            }

        }

    }
    public CacheManager setCacheMapSize(int size) {

        try {
            if (checkSize(size).equals("pass")) {
                cacheMapSize = size;
            }
        } catch (CacheMapSizeException e) {
            System.out.println("CacheMapSizeException:  " + e);
        }
        return this;
    }

    public CacheManager setCacheMapLength(int length) {
        try {
            if (checkSize(length).equals("pass")) {
                cacheMapLength = length;
            }
        } catch (CacheMapSizeException e) {
            System.out.println("CacheMapSizeException:  " + e);
        }


        return this;
    }

    private String checkSize(int size) throws CacheMapSizeException {
        {
            if (size >= 0) {
                return "pass";
            } else {
                throw new CacheMapSizeException("The size can not be less than zero");
            }
        }
    }

    /**
     * cacheMap：内存缓存集合
     *
     * @author LiXaing
     * create at 2017/3/28 12:10
     */
    private ArrayMap<String, MemoryCacheIdentityInformationBean> cacheMap = new ArrayMap<String, MemoryCacheIdentityInformationBean>();

    private ArrayMap<String, String> cacheAliasMap = new ArrayMap<String, String>();


    /**
     * @apiNote cacheIdentityInformation：每一条cahe的Information
     * @author LiXaing
     * create at 2017/3/28 12:10
     */
    private LinkedList<String> cacheIdentityInformation = new LinkedList<String>();

//

    public void putAlias(String alias,String value) {
        if (alias == null) return;

        synchronized (this){
        if (!cacheAliasMap.containsKey(alias)) {
            cacheAliasMap.put(alias,value);
        }
        }
    }
    public String getAlias(String alias){
           return cacheAliasMap.get(alias);
    }

    public void removeAlias(String alias){
        synchronized (this){
                cacheAliasMap.remove(alias);
//            if (cacheAliasMap.containsKey(alias)) {
//            }
        }
    }







    /**
     * @apiNote 记录当前cacheItem的IdentityInformation(身份信息)
     * @param key ：key
     * @author LiXaing
     * create at 2017/3/28 17:39
     */
    private void putInformation(String key) {
        if (cacheIdentityInformation.contains(key)) {
            cacheIdentityInformation.remove(key);
                cacheIdentityInformation.addLast(key);
        }else {
            cacheIdentityInformation.addLast(key);
        }

    }


    /**
    * @apiNote :将当前cacheItem的IdentityInformation(身份信息)从<var>cacheIdentityInformation</var>中移除
     * @param key :key
    *@author LiXaing
    *create at 2017/3/28 20:19
    */
    private void removeInformation(String key) {

                cacheIdentityInformation.remove(key);
    }





    /**
     * @apiNote put：添加一条数据到缓存集合
     *
     * @param key   : key
     * @param value : value
     * @param validTime : 有效期时间；单位 min（分钟）
     * @param createTime: 创建时间；
     * @param lastTime: 最后访问时间；
     * @author LiXaing
     * create at 2017/3/28 15:42
     */
    public  void put(String key, String value,int validTime, long createTime, long lastTime) throws DataSpecificationException {
        if (key == null || value == null ) {
            throw new NullPointerException("key == null || value == null ");
        }
        if (validTime < -1 || validTime == 0 || createTime < 0 || lastTime < 0 ){

            throw new DataSpecificationException(" validTime < -1 || validTime == 0 || createTime < 0 || lastTime < 0 ");
        }
//        判断是否需要添入集合
        synchronized(this){

        if (checkCacheMap(key)) {

            cacheMap.put(key, new MemoryCacheIdentityInformationBean(value, validTime, createTime, lastTime, getCacheItemSize(value)));
            putInformation(key);

        }
        }
    }

    private void checkCacheMapSize(long itemSize) {

        nowCacheMapSize = nowCacheMapSize + itemSize;

        long mCacheMapSize = cacheMapSize;

        if (nowCacheMapSize  > mCacheMapSize && cacheMap.size()>0 && cacheIdentityInformation.size() >0) {


            while (nowCacheMapSize  > mCacheMapSize){
            nowCacheMapSize = nowCacheMapSize - remove(cacheIdentityInformation.getFirst()).getItemSize();
            }
        }
    }

    private long getCacheItemSize(String value) {
        long itemSize= 32*2+4+8+8+(value.length()*2);
        checkCacheMapSize(itemSize);
        return itemSize;
    }

    public  String get(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

//        判断是否需要添入集合
        synchronized(this){
            MemoryCacheIdentityInformationBean m = cacheMap.get(key);
            if (m != null) {
                long nowTimeMillis = System.currentTimeMillis();

                long validTime = m.getValidTime();
//                当为-404，当前item已过期或者系统时间有变动
                if (((validTime) == -404)) {
                        return null;
                }
                //            当有效期-1为默认值，设定为一直有效
                if (!((validTime) == -1)) {
                long lastTime = m.getLastTime();

//                当最后访问时间小于当前访问时间，时间有变动就清除当前缓存信息，避免未知的缓存异常
                    if (!(lastTime< nowTimeMillis)) {
//                        将ValidTime设置为-404，防止多线程并发时，又读取当前数据进行返回
                        m.setValidTime(-404);
                        deleteCacheItemAll(key);
                        return null;
                    }


//            判断是否逾有效期
                    if ((((lastTime)+(validTime*60*1000)) < nowTimeMillis)) {
                        m.setValidTime(-404);
                            deleteCacheItemAll(key);
                        return null;
                    }
                }

//                改变最后访问时间
                m.setLastTime(nowTimeMillis);

                putInformation(key);



//                改变最后访问时间
                new Thread(()->{
                File file = FileUtil.getDiskCacheDir(context,
                         key);
                        writeLoaclCache(file,m.getValue(),m.getValidTime(),m.getCreateTime(),System.currentTimeMillis());
                }).start();

        return m.getValue();
            }


        return null;
        }
    }




    /**
     * @apiNote remove：将指定的一条数据从缓存集合中移除
     * @param key   : key
     * @author LiXaing
     * create at 2017/3/28 15:42
     */
//
    public  MemoryCacheIdentityInformationBean remove(String key) {
        MemoryCacheIdentityInformationBean cacheMapItem = null;
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        synchronized(this){

        if (cacheMap.containsKey(key)) {
            cacheMapItem = cacheMap.remove(key);
            removeInformation(key);
            if (cacheAliasMap.containsValue(key)) {
            Set<Map.Entry<String, String>> entries = cacheAliasMap.entrySet();
            Iterator<Map.Entry<String,String>> it = entries.iterator();
            while(it.hasNext()){
                Map.Entry<String, String> mey = it.next();
                //getKey()和getValue是接口Map.Entry<K,V>中的方法，返回对应的键和对应的值
                String mkey = mey.getKey();
                String mvalue = mey.getValue();
                if (mkey != null && mvalue!= null ) {

                if (mvalue.equals(key)) {
                    removeAlias(mkey);
                }
                }
            }

            }
        }
        }
        return cacheMapItem;
    }


    /**
     * 清空内存缓存
     *@author LiXaing
     *create at 2017/4/26 23:47
     */
    public synchronized void cleanMemory() {
        cacheMap.clear();
        cacheAliasMap.clear();
        cacheIdentityInformation.clear();
    }

    /**
    * 清空指定目录名称下的缓存目录
    *@author LiXaing
    *create at 2017/4/27 0:03
    */
    public void cleanLocal(String UserName){
        FileUtil.deleteDirectory(new File(FileUtil.getDiskCacheDirPath(context)).getParentFile().getAbsolutePath() + File.separator + UserName);
    }

    /**
    * 清空硬盘中所有的缓存
    *@author LiXaing
    *create at 2017/4/27 0:09
    */
    public void cleanLocalAll(){
        FileUtil.deleteDirectory(new File(FileUtil.getDiskCacheDirPath(context)).getParentFile().getAbsolutePath());
    }


    /**
    * 清空内存，硬盘中所有的缓存
    *@author LiXaing
    *create at 2017/4/27 0:11
    */
    public void cleanAll(){
        cleanMemory();
        cleanLocalAll();
    }











    public synchronized void deleteCacheItemLocal(String key) {

        File file = FileUtil.getDiskCacheDir(context,
                 key);
        file.delete();
    }
    public  void deleteCacheItemAll(String key) {
        deleteCacheItemLocal(key);
        remove(key);
    }






    private boolean checkCacheMap(String key) {
//        判断是否存在
        if (!cacheMap.containsKey(key)) {
//                当不存在
                FreeCacheMap();
                return true;
        }else {
            remove(key);
                return true;
        }
    }

    //    判断当前的集合是否需要释放相应内存
    private void FreeCacheMap() {

        if (cacheMap.size() >= cacheMapLength && cacheMap.size() >0) {

                while (cacheMap.size() >= cacheMapLength && cacheMap.size() >0){
                    nowCacheMapSize = nowCacheMapSize - remove(cacheIdentityInformation.getFirst()).getItemSize();
                }
        }
    }



    //写数据到SD中的文件
    public synchronized void writeLoaclCache(File file,String cache,int validTime, long createTime, long lastTime) {
        //创建Properties
        Properties prop  = new Properties();

        //设置Properties中的属性
        prop.setProperty("validTime",validTime+"");
        prop.setProperty("createTime",createTime+"");
        prop.setProperty("lastTime",lastTime+"");
        prop.setProperty("cache",cache);

        try{
            FileOutputStream fos = new FileOutputStream(file);
            prop.store(fos, null);
            fos.close();
        }

        catch(Exception e){
            System.out.println("写入错误：file.delete();");
            file.delete();
            e.printStackTrace();
        }

        chechLocalSize();
    }


    //读SD中的文件
    public  Bundle readLoaclCache(File file)  {

        if (!file.exists() || file.length() <1) {
//            file.delete();
            return null;
        }
        String createTime;
        String validTime;
        String lastTime;
        String cache;

         FileInputStream fis = null;
            //创建一个Properties
            Properties prop = new Properties();
        try{
            fis = new FileInputStream(file);

            //获取配置信息
            prop.load(fis);
                fis.close();
        }

        catch(Exception e){
//			当读取本地数据出现异常，默认返回为null
            e.printStackTrace();
            System.out.println("prop.load fise.printStackTrace() : -> " + e.getMessage());
            file.delete();
            return null;
        }
             createTime  = prop.getProperty("createTime");
             validTime   = prop.getProperty("validTime");
             lastTime    = prop.getProperty("lastTime");
             cache       = prop.getProperty("cache");

//            当有效期-1为默认值，设定为一直有效
            if (!(Integer.parseInt(validTime) == -1)) {
                System.out.println("不等于-1");

//                当最后访问时间小于当前访问时间，时间有变动就清除当前缓存信息，避免未知的缓存异常
            if (!(Long.parseLong(lastTime) < System.currentTimeMillis())) {

                System.out.println("Long.parseLong(lastTime) < System.currentTimeMillis()");
                file.delete();
                return null;
            }


//            判断是否逾有效期
            if ((((Long.parseLong(lastTime))+(Long.parseLong(validTime)*60*1000)) < System.currentTimeMillis())) {
                file.delete();
                return null;
            }

            }

//            更新当前的最后访问时间
            new Thread(()->{
                synchronized (this){


                 FileOutputStream fos  = null;
//                Properties prop2 = new Properties();
                long l = 0;
                try {
                    fos = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                 l = System.currentTimeMillis();
                prop.setProperty("lastTime", l + "");
                synchronized (this){
                try {
                    prop.store(fos, null);
                        fos.close();
                } catch (IOException e) {
                    System.out.println("IOExceptionsetProperty(lastTime");
                    file.delete();
                    e.printStackTrace();
                }
                }}}).start();




        Bundle bundle = new Bundle();

        bundle.putString("validTime",validTime);
        bundle.putString("createTime",createTime);
        bundle.putString("lastTime",System.currentTimeMillis()+"");
        bundle.putString("cache", cache);

        return bundle;
    }

//    {@link #put(Object, Object)} of all key/value pairs in <var>array</var>
}
