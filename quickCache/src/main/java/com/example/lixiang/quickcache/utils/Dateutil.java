package com.example.lixiang.quickcache.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lixiang on 2017/3/28.
 */
public class Dateutil {

    public  static long dateToInt(){
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");//可以方便地修改日期格式
        return Long.parseLong(dateFormat.format( now ));
    }



}
