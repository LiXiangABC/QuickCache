# QuickCache
- 添加QucikCache之前动态GIF展示　　　　　-添加QucikCache之后动态GIF展示


![缓存前](https://github.com/LiXiangABC/QuickCache/blob/master/SimpleGIF/before.gif)![缓存后](https://github.com/LiXiangABC/QuickCache/blob/master/SimpleGIF/after.gif)



# 用法
- AndroidStudio
```java  
  
allprojects {
    repositories {
        jcenter()
        <!--在项目的根gradle下添加如下这行代码-->
        maven{url 'https://jitpack.io'}
    }
}
  
```
```java  
<!--项目的gradle下添加-->
compile 'com.github.LiXiangABC:QuickCache:v2.0.0'
  
```

## １.初始化QucikCache

  ##### 在　**Application**　中对　**QuickCacheUtil**　进行初始化
```java
QuickCacheUtil.getInstance().setContext(getApplication();
```

## ２.使用QucikCache
##### 示例：


```java
<!--设置请求参数-->
LinkedHashMap<String, String> mLinkedHashMap = new LinkedHashMap<String, String>();
        mLinkedHashMap.put("ordercode", getArguments().getString("ordercode"));
        
        <!--调用LoadingCacheString()方法进行相关参数的设定-->
        QuickCacheUtil.getInstance().LoadingCacheString()
        .setTag(this)
        .setAlias(NetWorkURLBean.QUEUE_ORDER_INFO + getArguments().getString("ordercode"))
        .setRequestType(QuickCacheUtil.RequestType.post)
        .setUrl(NetWorkURLBean.QUEUE_ORDER_INFO)
        .setParams(mLinkedHashMap)
        .setIsOpenNetWork(true)
        .setIsRefreshCache(false)
        .setOrc(new onResponseCacheListener() {
                    @Override
                    public void onResponseCache(String onResponseData) {
                        <!--在此执行获取到数据 onResponseData 的后续操作-->
                    }
                }
            ).commit();
```
### 1.请求用到的API
##### **QuickCacheUtil.getInstance().LoadingCacheString()** 返回的 **LoadingCacheStringBean** API说明



API | 描述
---|---
setTag(Object tag) | 为当前的请求设定一个标记，当展示页面被关闭，就会依据Tag取消当前网络请求，防止内存泄漏。
setAlias(String alias) | 为当前的请求设定一个别名，通过别名可以在内存或者本地硬盘中找到这条CacheItem。
setValidTime(int validTime)|为当前请求获取到的数据设定有效期时间(1=1min;  默认为8*60 -> 8个小时)
setRequestType(QuickCacheUtil.RequestType requestType)|为请求设定请求类型，QuickCacheUtil.RequestType为枚举，有四种枚举类型：post,get,put,delete。
setUrl(String url)| URL地址
setParams(Map<String, String> params)|设定请求参数，当没有参数的时候可以设定为null。
setIsOpenNetWork(boolean isOpenNetWork)|内部封装了okhttp的网络请求，使用中有自己的网络封装，可不设定该参数，默认值为false(不使用QucilCache中的网络请求)。
setIsRefreshCache(boolean isRefreshCache)|为true时,直接调用QucilCache中的网络请求获取数据。为false，先访问内存缓存，有就返回数据，没有就访问本地硬盘，有就返回数据并且将数据加载入内存，当本地没有并且IsOpenNetWork为false，就返回null，当本地没有并且IsOpenNetWork为true时就进行网络访问。
setOrc(onResponseCacheListener orc)|设定获取到数据后的回调操作。 
commit()| 对相关参数设定完成后，提交当前请求操作。(最后一定要记得commit否则没有响应)。

### 2.对内存管理用到的一些API
##### **QuickCacheUtil.getCacheManager()** 返回的 **CacheManager** 提供的一些API说明


API | 描述
---|---
setCacheMapLength(int length) | 设定内存中存储的最大CacheItem数量，默认值为1000。
setCacheMapSize(int size) | 设定对内存占用的MaxSize，默认值为内存的1/16。
setlocalSize(int size)|设定本地的缓存区域大小，默认为10M，1=1M。
