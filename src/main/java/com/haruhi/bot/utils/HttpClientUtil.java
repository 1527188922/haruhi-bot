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

@Slf4j
public class HttpClientUtil {
    private HttpClientUtil(){}
    private static CloseableHttpClient defaultHttpClient = HttpClientBuilder.create().build();

    public static String doGet(String url, Map<String,Object> urlParams,int timeout){
        CloseableHttpClient httpClient = null;
        if(timeout > 0){
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout)
                    .setStaleConnectionCheckEnabled(true)
                    .build();
            httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .build();
        }else{
            httpClient = defaultHttpClient;
        }

        CloseableHttpResponse response = null;
        try {
            String uri = "";
            if(urlParams != null){
                uri = RestUtil.urlSplicing(url,urlParams);
            }else{
                uri = url;
            }
            HttpGet httpGet = new HttpGet(uri);
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity,"UTF-8");
        } catch (IOException e) {
            log.error("HttpClient请求{}异常",url,e);
            return null;
        }
    }
}
