package com.haruhi.bot.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HttpClientUtil {
    private HttpClientUtil(){}
    private static CloseableHttpClient defaultHttpClient = HttpClientBuilder.create().build();
    public static CloseableHttpClient getHttpClient(){
        return defaultHttpClient;
    }

    private static Map<Integer,CloseableHttpClient> httpClientCache = new ConcurrentHashMap<>();

    public static CloseableHttpClient getHttpClient(int timeout){

        CloseableHttpClient httpClient = httpClientCache.get(timeout);
        if(httpClient != null){
            return httpClient;
        }
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(timeout)
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setStaleConnectionCheckEnabled(true)
                .build();
        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        httpClientCache.put(timeout,httpClient);
        return httpClient;
    }


    public static String doGet(CloseableHttpClient httpClient,String url, Map<String,Object> urlParams){

        CloseableHttpResponse response = null;
        try {
            String uri = "";
            if(urlParams != null){
                uri = RestUtil.urlSplicing(url,urlParams);
            }else{
                uri = url;
            }
            HttpGet httpGet = new HttpGet(encode(uri));
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity,"UTF-8");
        } catch (IOException e) {
            log.error("HttpClient请求{}异常",url,e);
            return null;
        }
    }

    public static String encode(String url) {
        return url.replace("\\","%5C")
                .replace("+","%2B")
                .replace(" ","%20")
                .replace("%","%25")
                .replace("#","%23")
                .replace("$","%24")
                .replace("^","%5E")
                .replace("{","%7B")
                .replace("}","%7D")
                .replace("|","%7C")
                .replace("[","%5B")
                .replace("]","%5D");

    }
}
