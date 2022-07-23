package com.haruhi.bot.ws;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.config.WebSocketConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.constant.PostTypeEnum;
import com.haruhi.bot.dto.response.Answer;
import com.haruhi.bot.dto.response.AnswerBox;
import com.haruhi.bot.extend.PluginSubject;
import com.haruhi.bot.handlers.command.Subject;
import com.haruhi.bot.thread.ReConnectTask;
import com.simplerobot.modules.utils.KQCodeUtils;
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
                String command = msgBody.getString("message");
                if(command != null){
                    Subject.update(msgBody,command);
                    PluginSubject.update(msgBody,command);
                    log.info("收到消息==>{}",message);
                }

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


    private static void sendMessage(String msg){
        try {
            Client.getInstance().session.getAsyncRemote().sendText(msg);
        } catch (Exception e){
            log.info("发送消息时异常:{}",e.getMessage());
        }
    }
    public static <T> void sendMessage(AnswerBox<T> box){
        sendMessage(JSONObject.toJSONString(box));
    }
    /**
     * @param target to
     * @param type 群聊 or 私聊
     * @param message 消息
     * @param action 动作类型 详见gocq文档 https://docs.go-cqhttp.org/api
     * @param autoEscape 是否不解析cq码 true:不解析 false:解析
     */
    public static void sendMessage(String target, MessageTypeEnum type, String message,GocqActionEnum action, boolean autoEscape){

        AnswerBox<Answer> box = new AnswerBox<>();
        Answer answer = new Answer();
        answer.setMessage(message);
        answer.setMessage_type(type.getType());
        answer.setUser_id(target);
        answer.setAuto_escape(autoEscape);
        box.setParams(answer);
        box.setAction(action.getAction());
        sendMessage(JSONObject.toJSONString(box));
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
