package com.example.lixiang.quickcache.okhttp.cookie;

import android.content.Context;
import android.text.TextUtils;

import org.kymjs.kjframe.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public final class SimpleCookieJar implements CookieJar {
    private final List<Cookie> allCookies = new ArrayList<Cookie>();

    private Context context;

    public SimpleCookieJar(Context context) {
        this.context = context;
    }
    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        allCookies.addAll(cookies);

        //先查询是否存在cookie  如果存在则不覆盖，不存在则覆盖
        //获取本地的cookie
        String s = PreferenceHelper.readString( context, "isLogin", "isLogin");
//        System.out.println("初始数据" + s);
        if (TextUtils.isEmpty(s)) {
            StringBuilder strBudilder = new StringBuilder();
            for (Cookie c : cookies
                    ) {
                strBudilder.append(c.name() + "`");
                strBudilder.append(c.value() + "`");
                strBudilder.append(c.expiresAt() + "`");
                strBudilder.append(c.domain() + "`");
                strBudilder.append(c.path() + "`");
                strBudilder.append(c.secure() + "`");
                strBudilder.append(c.hostOnly() + "`");
                strBudilder.append(c.httpOnly() + "`");
                strBudilder.append(c.persistent() + "`");
                strBudilder.append("\n");
            }
            //把cookie保存到本地做持久化
            PreferenceHelper.write(context, "isLogin", "isLogin", strBudilder.toString());
            System.out.println("Cookie:"+strBudilder.toString());
        }
        
        
        
        
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url) {
//        List<Cookie> result = new ArrayList<Cookie>();
//        for (Cookie cookie : allCookies) {
//            System.out.println("++++++++++++++++++" + cookie.toString());
//            if (cookie.matches(url)) {
//                result.add(cookie);
//            }
//        }
    	
    	
    	 List<Cookie> allCookies = new ArrayList<>();
         String s = PreferenceHelper.readString(context, "isLogin", "isLogin");
         if (s != null) {
             s = s.trim();
             for (String x : s.split("\n")) {
                 String[] cookiex = x.split("`");

                 for (int i = 0; i < cookiex.length; i++) {
                     if (i != 3) {
                         Cookie.Builder builder = new Cookie.Builder();
                         builder.name(cookiex[0].trim());
                         builder.value(cookiex[1]);
                         builder.expiresAt(Long.parseLong(cookiex[2]));
                         builder.domain(cookiex[3]);
                         builder.path(cookiex[4]);
                         Cookie build = builder.build();
                         allCookies.clear();
                         allCookies.add(build);
                     }
                 }

             }

         }
//         System.out.println("读取到的本地的Cook："+s);

//         访问网络，先访问本地
         List<Cookie> result = new ArrayList<>();

         for (Cookie cookie : allCookies) {
             if (cookie.matches(url)) {
                 result.add(cookie);
             }
         }
        return result;
    }
}
