package com.ccuix.ShanBay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.ccuix.Entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class ShanBayCollection {
    @Value("${shanbay.collectionUrl}")
    private String collectionUrl;
    @Value("${shanbay.getWordUrl}")
    public String getWordUrl;
    @Value("${shanbay.cookie}")
    private String cookie;
    @Value("${decodeUrl}")
    private String decodeUrl;

    /**
     * Avoid repeated additions, as they count Sbay as new words
     * Because Sbay has a special strategy for new words
     */
    private static Set<YouDaoWordItem> set = new HashSet<>();

    public void addWordToShanBay(YouDaoData youDaoData) {
        List<YouDaoWordItem> itemList = youDaoData.getItemList();
        List<YouDaoWordItem> neededAddWords = new ArrayList<>();
        itemList.forEach(item -> {
            if (!set.contains(item)) {
                set.add(item);
                neededAddWords.add(item);
            }
        });
        if (neededAddWords.size() == 0) {
            return;
        }
        List<String> wordIds;
        // trans word to wordId(encoding) of shanBay
        wordIds = RequestWordIds(itemList);
        // trans the encoded wordId to wordId and word 
        List<ShanBayWordPair> shanBayWordPairList = Decoding(wordIds);
        
        // add words to shanBay notebook
        Collection(shanBayWordPairList);
    }

    /**
     * @param itemList
     * @return {@link List }<{@link String }>
     * trans word to wordId(encoding) of shanBay
     */
    public List<String> RequestWordIds(List<YouDaoWordItem> itemList) {
        HttpClient httpClient = new HttpClient();
        List<String> result = new ArrayList<>();
        itemList.forEach(youDaoWordItem -> {
            try {
                Thread.sleep(500);
                // create a getMethod
                GetMethod getMethod = getGetMethod(youDaoWordItem);
                httpClient.executeMethod(getMethod);
                // trans res to obj
                String data = new String(getMethod.getResponseBody());
                // the word exist in youdao, but it not exists in shanbay
                // especially for phrases
                // So we need to judge, or we'll get an error when we decode
                // if a word is not existing in shanbay , it will return the json below.
                // we needed append a '\n' after return data , I suspect the reason is related to Fastjson.
                log.info("add word {} to Sbay", youDaoWordItem.getWord());
                if (!data.equals("{\"errors\":{},\"msg\":\"not found vocabulary\"}\n")) {
                    ShanBayGetWordData wordData = JSONObject.parseObject(data, ShanBayGetWordData.class);
                    // add the return list 
                    result.add(wordData.getData());
                } else {
                    log.error("this word :{} is not exist", youDaoWordItem.getWord());
                }
                // releaseConnection
                getMethod.releaseConnection();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }


    /**
     * @param wordsData
     * @return {@link List }<{@link ShanBayWordPair }>
     * we need to decode the wordsData because of wordsData from return by shanBay is encoded;
     */
    public List<ShanBayWordPair> Decoding(List<String> wordsData) {
        HttpClient httpClient = new HttpClient();
        List<ShanBayWordPair> result = new ArrayList<>();
        // item is encoding data 
        wordsData.forEach(item -> {
            try {
                Thread.sleep(300);
                PostMethod postMethod = new PostMethod(decodeUrl);
                // add post body 
                RequestEntity requestEntity = getRequestEntity(item);
                postMethod.setRequestEntity(requestEntity);
                // send post request 
                httpClient.executeMethod(postMethod);
                // trans the res to obj 
                String shanBayWord = new String(postMethod.getResponseBody(), "UTF-8");
                ShanBayWordPair shanBayWordPair = JSON.parseObject(shanBayWord, ShanBayWordPair.class);
                result.add(shanBayWordPair);
                postMethod.releaseConnection();
            } catch (IOException | InterruptedException | JSONException e) {
                log.error("json parse is error");
            }
        });
        return result;
    }


    /**
     * @param wordPairs add word to shanBay Collection Book
     */
    public void Collection(List<ShanBayWordPair> wordPairs) {
        HttpClient httpClient = new HttpClient();
        wordPairs.forEach(item -> {
            try {
                PostMethod postMethod = getPostRequest(item.getWordId());
                httpClient.executeMethod(postMethod);
                postMethod.getStatusCode(); // get http return code 
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class Word {
        public String word;
    }

    private GetMethod getGetMethod(YouDaoWordItem youDaoWordItem) {
        String word = youDaoWordItem.getWord();
        StringBuilder switchSpace = new StringBuilder();
        // url can't exist space , we need to trans space to "%20"
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == ' ') {
                switchSpace.append("%20");
            } else {
                switchSpace.append(word.charAt(i));
            }
        }
        GetMethod getMethod = new GetMethod(getWordUrl + switchSpace);
        getMethod.addRequestHeader("Cookie", cookie);
        return getMethod;
    }

    private RequestEntity getRequestEntity(String item) throws UnsupportedEncodingException {
        Word word = new Word();
        word.word = item;
        String input = JSON.toJSONString(word);
        RequestEntity requestEntity = new StringRequestEntity(input, "application/json", "UTF-8");
        return requestEntity;
    }

    private PostMethod getPostRequest(String item) throws UnsupportedEncodingException {
        PostMethod postMethod = new PostMethod(collectionUrl);
        postMethod.setRequestHeader("cookie", cookie);
        ShanBayPostWord word = new ShanBayPostWord(item);
        String shanBayWord = JSON.toJSONString(word);
        RequestEntity requestEntity = new StringRequestEntity(shanBayWord, "application/json", "UTF-8");
        postMethod.setRequestEntity(requestEntity);
        return postMethod;
    }

}
