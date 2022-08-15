package com.haruhi.bot.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

public class HttpClientUtil {
    private HttpClientUtil(){}
    private static CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    public static String doGet(String url, Map<String,Object> urlParams){
        CloseableHttpResponse response = null;
        try {
            String uri = "";
            if(urlParams != null){
                StringBuffer sb=new StringBuffer("?");
                for(Map.Entry<String,Object> map:urlParams.entrySet()){
                    sb.append(map.getKey()+"="+(map.getValue())+"&");
                }
                uri = url.concat(sb.substring(0, sb.length() - 1));
            }else{
                uri = url;
            }
            HttpGet httpGet = new HttpGet(uri);
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity,"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
