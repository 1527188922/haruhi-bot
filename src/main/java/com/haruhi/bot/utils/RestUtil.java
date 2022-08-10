package com.haruhi.bot.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class RestUtil {
    private RestUtil(){}
    public static <T> T sendGetRequest(RestTemplate restTemplate, String url,Map<String, Object> urlRequestParam, Class<T> type){
        return RestUtil.sendRequest(restTemplate,url,HttpMethod.GET,null,urlRequestParam,type);
    }
    public static <T,O> T sendPostRequest(RestTemplate restTemplate, String url,O msgBody, Map<String, Object> urlRequestParam, Class<T> type){
        return RestUtil.sendRequest(restTemplate,url,HttpMethod.POST,msgBody,urlRequestParam,type);
    }
    private static <T,O> T sendRequest(RestTemplate restTemplate, String url,HttpMethod method ,O msgBody, Map<String, Object> urlRequestParam, Class<T> type){
        try {
            // 设置请求头
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<O> entity = new HttpEntity<>(msgBody, httpHeaders);
            ResponseEntity<String> response = null;
            if(urlRequestParam != null){
                StringBuffer sb=new StringBuffer("?");
                for(Map.Entry<String,Object> map:urlRequestParam.entrySet()){
                    sb.append(map.getKey()+"="+(map.getValue())+"&");
                }
                response = restTemplate.exchange(url.concat(sb.substring(0, sb.length() - 1)), method, entity, new ParameterizedTypeReference<String>() {
                });
            }else{
                response = restTemplate.exchange(url, method,entity,new ParameterizedTypeReference<String>() {
                });
            }

            return processResponse(response,type);
        }catch (Exception e){
            log.info("rest请求发送异常",e);
            return null;
        }
    }

    /**
     * form提交
     * 可发送文件
     * @param restTemplate
     * @param url
     * @param param
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T sendPostForm(RestTemplate restTemplate, String url, LinkedMultiValueMap<String,Object> param,Class<T> type){
        ResponseEntity<String> response = restTemplate.postForEntity(url, param, String.class, (Object) null);
        return processResponse(response,type);
    }

    private static <T> T processResponse(ResponseEntity<String> response,Class<T> tClass){
        if(response == null ){
            log.info("http请求响应结果为空 ResponseEntity == null");
            return null;
        }
        if(response.getStatusCodeValue() != 200){
            log.info("http请求响应状态码异常:{}\n{}",response.getStatusCode().value(),response);
            return null;
        }
        String body = response.getBody();
        if(body == null){
            log.info("接口响应结果为null");
            return null;
        }

        if(tClass == String.class){
            return (T)body;
        }
        try {
            return JSONObject.parseObject(body, tClass);
        }catch (Exception e){
            log.error("请求结果(json串)转javabean异常",e);
            return null;
        }
    }

    /**
     * 获取指定连接时间的RestTemplate
     * @param timeout
     * @return
     */
    public static RestTemplate getRestTemplate(int timeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        RestTemplate restTemp = new RestTemplate();
        restTemp.setRequestFactory(requestFactory);
        restTemp.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemp;
    }
}
