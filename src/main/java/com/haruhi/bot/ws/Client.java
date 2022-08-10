package com.haruhi.bot.ws;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.config.WebSocketConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.constant.PostTypeEnum;
import com.haruhi.bot.dto.gocq.response.HttpResponse;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.Answer;
import com.haruhi.bot.dto.gocq.request.AnswerBox;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.dispenser.message.MessageDispenser;
import com.haruhi.bot.thread.ReConnectTask;
import com.haruhi.bot.utils.RestUtil;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.List;

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
    private static Client instance = null;
    public synchronized static Client getInstance() {
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
    public void onMessage(final String message){
        try {
            Message messageBean = JSONObject.parseObject(message, Message.class);

            if(PostTypeEnum.message.toString().equals(messageBean.getPost_type())){
                // 普通消息
                final String command = messageBean.getMessage();
                if(command != null){
                    MessageDispenser.onEvent(messageBean,command);
                }

            }else if(PostTypeEnum.notice.toString().equals(messageBean.getPost_type())){
                // bot通知
            } else if(PostTypeEnum.meta_event.toString().equals(messageBean.getPost_type())){
                // 系统消息 心跳包、
            }else{

            }
        }catch (Exception e){
            log.error("收到消息时发生异常",e);
        }
    }

    private static void sendMessage(String msg){
        try {
            Client.getInstance().session.getAsyncRemote().sendText(msg);
        } catch (Exception e){
            log.error("发送消息时异常",e);
        }
    }
    public static <T> void sendMessage(AnswerBox<T> box){
        sendMessage(JSONObject.toJSONString(box));
    }
    /**
     * @param target to
     * @param groupId 群号
     * @param type 群聊 or 私聊
     * @param message 消息
     * @param action 动作类型 详见gocq文档 https://docs.go-cqhttp.org/api
     * @param autoEscape 是否不解析cq码 true:不解析 false:解析
     */
    public static void sendMessage(String target,String groupId ,MessageTypeEnum type, String message,GocqActionEnum action, boolean autoEscape){

        AnswerBox<Answer> box = new AnswerBox<>();
        Answer answer = new Answer();
        answer.setMessage(message);
        answer.setMessage_type(type.getType());
        answer.setUser_id(target);
        answer.setAuto_escape(autoEscape);
        answer.setGroup_id(groupId);
        box.setParams(answer);
        box.setAction(action.getAction());
        sendMessage(JSONObject.toJSONString(box));
    }
    public static void sendMessage(GocqActionEnum action,String groupId, List<ForwardMsg> params){
        AnswerBox<Answer> collectionAnswerBox = new AnswerBox<>();

        collectionAnswerBox.setAction(action.getAction());

        Answer answer = new Answer();
        answer.setGroup_id(groupId);
        answer.setMessages(params);
        collectionAnswerBox.setParams(answer);
        sendMessage(JSONObject.toJSONString(collectionAnswerBox));
    }
    public static HttpResponse sendRestMessage(GocqActionEnum action,String groupId, List<ForwardMsg> params){
        Answer answer = new Answer();
        answer.setGroup_id(groupId);
        answer.setMessages(params);
        return RestUtil.sendPostRequest(RestUtil.getRestTemplate(), BotConfig.HTTP_URL + "/" + action.getAction(),answer,null, HttpResponse.class);
    }
    public static void sendMessage(String target,String groupId ,String type, String message,GocqActionEnum action, boolean autoEscape){

        AnswerBox<Answer> box = new AnswerBox<>();
        Answer answer = new Answer();
        answer.setMessage(message);
        answer.setMessage_type(type);
        answer.setUser_id(target);
        answer.setGroup_id(groupId);
        answer.setAuto_escape(autoEscape);
        box.setParams(answer);
        box.setAction(action.getAction());
        sendMessage(JSONObject.toJSONString(box));
    }

    /**
     * http发送消息
     * 用于需要知道结果的发送
     * @param target
     * @param groupId
     * @param type
     * @param message
     * @param action
     * @param autoEscape
     * @return
     */
    public static String sendRestMessage(String target,String groupId ,String type, String message,GocqActionEnum action, boolean autoEscape){
        Answer answer = new Answer();
        answer.setMessage(message);
        answer.setMessage_type(type);
        answer.setUser_id(target);
        answer.setGroup_id(groupId);
        answer.setAuto_escape(autoEscape);
        return RestUtil.sendPostRequest(RestUtil.getRestTemplate(), BotConfig.HTTP_URL + "/" + action.getAction(),answer,null, String.class);
    }
    public static String sendRestMessage(String target,String groupId ,MessageTypeEnum type, String message,GocqActionEnum action, boolean autoEscape){
        Answer answer = new Answer();
        answer.setMessage(message);
        answer.setMessage_type(type.getType());
        answer.setUser_id(target);
        answer.setGroup_id(groupId);
        answer.setAuto_escape(autoEscape);
        return RestUtil.sendPostRequest(RestUtil.getRestTemplate(), BotConfig.HTTP_URL + "/" + action.getAction(),answer,null, String.class);
    }

    @OnClose
    public void onClose(Session session){
        log.info("连接断开,请检查go-cqhttp是否正常启动;检查go-cqhttp ws配置是否与bot一致;开始重连...");
        reConnection();
    }

    @OnError
    public void onError(Session session,Throwable error){
        log.error("连接发生异常",error);
        reConnection();
    }

    public static void reConnection(){
        instance = null;
        ReConnectTask.execute();
    }
}
