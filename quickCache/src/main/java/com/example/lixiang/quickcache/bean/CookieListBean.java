package com.example.lixiang.quickcache.bean;

import java.util.List;

/**
 * Created by lixiang on 2017/6/22.
 */
public class CookieListBean {


    /**
     * channel_id : 2
     * channel_name : 洗美
     */

    private List<CookieBean> cookieBean;

    public List<CookieBean> getChannels() {
        return cookieBean;
    }

    public void setChannels(List<CookieBean> channels) {
        this.cookieBean = channels;
    }

    public static class CookieBean {
        private  String name;
        private  String value;
        private  long expiresAt;
        private  String domain;
        private  String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public long getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(long expiresAt) {
            this.expiresAt = expiresAt;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }
}
