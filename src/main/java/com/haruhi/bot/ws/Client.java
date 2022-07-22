package com.haruhi.bot.ws;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.config.WebSocketConfig;
import com.haruhi.bot.constant.PostTypeEnum;
import com.haruhi.bot.handlers.command.Subject;
import com.haruhi.bot.thread.ReConnectTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

/**
 * ws正向代理
 * gocq作为服务端
 * bot作为客户端
 */
@Slf4j
@ClientEndpoint
public class Client {

    private Session session;
    private Client(String url) throws DeploymentException, IOException {
        session = ContainerProvider.getWebSocketContainer().connectToServer(this, URI.create(url));
    }
    public static Client instance = null;
    public static Client getInstance() {
        try {
            if(instance == null){
                instance = new Client(WebSocketConfig.GOCQ_WS);
            }
            return instance;
        }catch (Exception e){
            log.error("连接失败:{}",e.getMessage());
            return null;
        }
    }

    @OnMessage
    public void onMessage(String message){
        if(Strings.isBlank(message)){
            log.info("收到空消息");
            return;
        }
        try {
            JSONObject msgBody = JSONObject.parseObject(message);
            String postType = msgBody.getString(PostTypeEnum.post_type.toString());
            if(PostTypeEnum.message.toString().equals(postType)){
                // 普通消息
                Subject.update(msgBody);
            }else if(PostTypeEnum.notice.toString().equals(postType)){
                // bot通知
            } else if(PostTypeEnum.meta_event.toString().equals(postType)){
                // 系统消息 心跳包、
            }else{
//                log.info("收到未知post_type消息:{}",message);
            }
        }catch (Exception e){
            log.error("收到消息时发生异常:",e);
        }



    }

    public static void sendMessage(String msg){

        try {
            Client.getInstance().session.getAsyncRemote().sendText(msg);
        } catch (Exception e){
            log.info("发送消息时异常:{}",e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session){
        log.info("连接断开,gocq可能宕机,开始重连...");
        reConnection();
    }

    @OnError
    public void onError(Session session,Throwable error){
        log.info("连接发生异常:{}",error.getMessage());
        reConnection();
    }

    public static void reConnection(){
        instance = null;
        ReConnectTask.execute();
    }
}
