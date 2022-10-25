package com.haruhi.bot.ws;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.constant.event.PostTypeEnum;
import com.haruhi.bot.constant.event.MetaEventEnum;
import com.haruhi.bot.dto.gocq.response.HttpResponse;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.Params;
import com.haruhi.bot.dto.gocq.request.RequestBox;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.SystemService;
import com.haruhi.bot.thread.OnEventTask;
import com.haruhi.bot.thread.ReConnectTask;
import com.haruhi.bot.utils.RestUtil;
import lombok.extern.slf4j.Slf4j;


import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private volatile static boolean connecting = false;
    private static Client INSTANCE;
    public synchronized static boolean connect(){
        try {
            if(INSTANCE == null){
                INSTANCE = new Client(BotConfig.WS_URL);
            }
            connecting = false;
            return true;
        }catch (Exception e){
            log.error("WebSocket连接失败:{}",e.getMessage());
            return false;
        }
    }
    public static boolean connected(){
        return INSTANCE != null && INSTANCE.session != null && INSTANCE.session.isOpen();
    }
    private static final Map<String,Object> accessTokenMap = new HashMap<>(1);
    static {
        accessTokenMap.put("access_token",BotConfig.ACCESS_TOKEN);
    }

    @OnOpen
    public void onOpen(Session session){
        log.info("WebSocket连接go-cqhttp成功:{}",BotConfig.WS_URL);
        SystemService.loadLoginInfo();
    }

    @OnMessage
    public void onMessage(final String message){
        try {
            final Message messageBean = JSONObject.parseObject(message, Message.class);
            if(PostTypeEnum.meta_event.toString().equals(messageBean.getPost_type()) && MetaEventEnum.heartbeat.toString().equals(messageBean.getMeta_event_type())){
                // 心跳包
                return;
            }
            ThreadPoolFactory.getEventThreadPool().execute(new OnEventTask(messageBean));
        }catch (Exception e){
            log.error("收到消息时发生异常,消息:{}",message,e);
        }
    }

    public static <T> void sendMessage(T box){
        String boxJson = null;
        try {
            boxJson = JSONObject.toJSONString(box);
            INSTANCE.session.getAsyncRemote().sendText(boxJson);
            log.info("bot发送了：{}",boxJson);
        } catch (Exception e){
            log.error("发送消息时异常,消息:{}",boxJson,e);
        }
    }
//    public static <T> void sendMessage(RequestBox<T> box){
//        sendMessage(box);
//    }
    /**
     * @param target to
     * @param groupId 群号
     * @param type 群聊 or 私聊
     * @param message 消息
     * @param action 动作类型 详见gocq文档 https://docs.go-cqhttp.org/api
     * @param autoEscape 是否不解析cq码 true:不解析 false:解析
     */
    public static void sendMessage(String target, String groupId , MessageEventEnum type, String message, GocqActionEnum action, boolean autoEscape){

        RequestBox<Params> box = new RequestBox<>();
        Params answer = new Params();
        answer.setMessage(message);
        answer.setMessage_type(type.getType());
        answer.setUser_id(target);
        answer.setAuto_escape(autoEscape);
        answer.setGroup_id(groupId);
        box.setParams(answer);
        box.setAction(action.getAction());
        sendMessage(box);
    }
    public static void sendMessage(){

    }
    public static void sendMessage(GocqActionEnum action,String id, List<ForwardMsg> params){
        RequestBox<Params> collectionAnswerBox = new RequestBox<>();

        collectionAnswerBox.setAction(action.getAction());

        Params params1 = new Params();
        params1.setGroup_id(id);
        params1.setUser_id(id);
        params1.setMessages(params);
        collectionAnswerBox.setParams(params1);
        sendMessage(collectionAnswerBox);
    }
    public static HttpResponse sendRestMessage(GocqActionEnum action,String id, List<ForwardMsg> params){
        Params params1 = new Params();
        params1.setGroup_id(id);
        params1.setUser_id(id);
        params1.setMessages(params);
        return sendRestMessage(params,HttpResponse.class,action);
    }
    public static void sendMessage(String target,String groupId ,String type, String message,GocqActionEnum action, boolean autoEscape){

        RequestBox<Params> box = new RequestBox<>();
        Params params = new Params();
        params.setMessage(message);
        params.setMessage_type(type);
        params.setUser_id(target);
        params.setGroup_id(groupId);
        params.setAuto_escape(autoEscape);
        box.setParams(params);
        box.setAction(action.getAction());
        sendMessage(box);
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
        Params params = new Params();
        params.setMessage(message);
        params.setMessage_type(type);
        params.setUser_id(target);
        params.setGroup_id(groupId);
        params.setAuto_escape(autoEscape);
        return sendRestMessage(params,String.class,action);
    }
    public static String sendRestMessage(String target, String groupId , MessageEventEnum type, String message, GocqActionEnum action, boolean autoEscape){
        Params params = new Params();
        params.setMessage(message);
        params.setMessage_type(type.getType());
        params.setUser_id(target);
        params.setGroup_id(groupId);
        params.setAuto_escape(autoEscape);
        return sendRestMessage(params,String.class,action);
    }

    private static <T,O> T sendRestMessage(O msgBody,Class<T> type,GocqActionEnum action){

        return RestUtil.sendPostRequest(RestUtil.getRestTemplate(), BotConfig.HTTP_URL + "/" + action.getAction(),msgBody,accessTokenMap,type);
    }

    @OnClose
    public void onClose(Session session){
        log.info("连接断开,请检查go-cqhttp是否正常启动;开始重连...");
        reConnection();
    }

    @OnError
    public void onError(Session session,Throwable error){
        log.error("连接发生异常,开始重连",error);
        reConnection();
    }

    public synchronized static void reConnection(){
        if(!connecting){
            connecting = true;
            try {
                INSTANCE.session.close();
                INSTANCE.session = null;
            }catch (Exception e){}
            INSTANCE = null;
            ReConnectTask.execute();
        }
    }
}
