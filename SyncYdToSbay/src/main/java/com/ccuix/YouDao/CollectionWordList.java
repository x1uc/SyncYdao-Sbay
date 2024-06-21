package com.ccuix.YouDao;

import com.alibaba.fastjson2.JSON;
import com.ccuix.Entity.YouDaoData;
import com.ccuix.Entity.YouDaoListResponse;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CollectionWordList {
    @Value("${youdao.cookie}")
    private String youdaoCookie;
    @Value("${youdao.collectionUrl}")
    private String collectionUrl;
    @Value("${user-agent}")
    private String user_agent;

    private final static HttpClient httpClient = new HttpClient();

    public YouDaoData getCollectionWordList() {
        // set get contain
        GetMethod getMethod = new GetMethod(collectionUrl);
        getMethod.addRequestHeader("Cookie", youdaoCookie);
        getMethod.addRequestHeader("User-Agent", user_agent);
        // send request 
        try {
            httpClient.executeMethod(getMethod);
        } catch (IOException e) {
            // todo print log 
            throw new RuntimeException(e);
        }
        // trans responseBody to Obj
        try {
            String s = new String(getMethod.getResponseBody(), "UTF-8");
            YouDaoListResponse youDaoListResponse = JSON.parseObject(s, YouDaoListResponse.class);
            getMethod.releaseConnection();
            return youDaoListResponse.getData();
        } catch (IOException e) {
            // todo print log 
            throw new RuntimeException(e);
        }
    }
}
