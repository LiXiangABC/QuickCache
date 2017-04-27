package com.example.lixiang.quickcache.okhttp.builder;

import com.example.lixiang.quickcache.okhttp.request.PostFormRequest;
import com.example.lixiang.quickcache.okhttp.request.RequestCall;
import com.example.lixiang.quickcache.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhy on 15/12/14.
 */
public class PostFormBuilder extends OkHttpRequestBuilder
{
    private List<FileInput> files = new ArrayList<FileInput>();

    @Override
    public RequestCall build()
    {
        return new PostFormRequest(url, tag, params, headers, files).build();
    }

    public PostFormBuilder addFile(String name, String filename, File file)
    {
    	if(file.exists())
    	{
    		if(FileUtil.getFileSize(file.getAbsolutePath().toString()) > 0)
    		{
    			System.out.println("文件的大小为："+FileUtil.getFileSize(FileUtil.getFileSize(file.getAbsolutePath().toString()))+";路径为："+file.getAbsolutePath().toString());
    			files.add(new FileInput(name, filename, file));
    		}
    	}
        return this;
    }

    public PostFormBuilder addFileList(List<FileInput > fileInput)
    {
                files.addAll(checkFileList(fileInput));
        return this;
    }

    private List<FileInput> checkFileList(List<FileInput> fileInputs) {
        List<FileInput> tempFileInputs = new ArrayList<>();
        for (int i = 0; i < fileInputs.size(); i++) {

            if(fileInputs.get(i).file.exists())
            {
                if(FileUtil.getFileSize(fileInputs.get(i).file.getAbsolutePath().toString()) > 0)
                    tempFileInputs.add(fileInputs.get(i));
            }
        }
        return tempFileInputs;
    }

    public static class FileInput
    {
        public String key;
        public String filename;
        public File file;

        public FileInput(String name, String filename, File file)
        {
            this.key = name;
            this.filename = filename;
            this.file = file;
        }

        @Override
        public String toString()
        {
            return "FileInput{" +
                    "key='" + key + '\'' +
                    ", filename='" + filename + '\'' +
                    ", file=" + file +
                    '}';
        }
    }

    //
    @Override
    public PostFormBuilder url(String url)
    {
        this.url = url;
        return this;
    }

    @Override
    public PostFormBuilder tag(Object tag)
    {
        this.tag = tag;
        return this;
    }

    @Override
    public PostFormBuilder params(Map<String, String> params)
    {
        this.params = params;
        return this;
    }

    @Override
    public PostFormBuilder addParams(String key, String val)
    {
        if (this.params == null)
        {
            params = new LinkedHashMap<String, String>();
        }
        params.put(key, val);
        return this;
    }

    @Override
    public PostFormBuilder headers(Map<String, String> headers)
    {
        this.headers = headers;
        return this;
    }


    @Override
    public PostFormBuilder addHeader(String key, String val)
    {
        if (this.headers == null)
        {
            headers = new LinkedHashMap<String, String>();
        }
        headers.put(key, val);
        return this;
    }


}
