//package com.haruhi.bot.ws;
//
//import com.alibaba.fastjson.JSONObject;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import javax.websocket.*;
//import javax.websocket.server.PathParam;
//import javax.websocket.server.ServerEndpoint;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * websocket服务类
// * 被@OnOopen...修饰的方法必须为 public void
// */
//@Slf4j
//@Component
//@ServerEndpoint(value = "/ws/{clientName}")
//public class Server {
//
//    // 在线客户端数量
//    private static AtomicInteger onlineTotal = new AtomicInteger(0);
//
//    // Map<clientName,WebSocketServer>
//    private static Map<String,Server> clients = new ConcurrentHashMap<>();
//
//    // Map<sessionId,clientName>
//    private static Map<String,String> sessionMap = new ConcurrentHashMap<>();
//
//    private Session session;
//
//    /**
//     * 客户端连接
//     * @param clientName
//     * @param session
//     */
//    @OnOpen
//    public void onOpen(@PathParam("clientName") String clientName,Session session){
//        this.session = session;
//        int total = onlineTotal.incrementAndGet();
//
//        clients.put(clientName,this);
//        sessionMap.put(session.getId(),clientName);
//
//        log.info("有客户端连接:{},当前在线数:{}",clientName,total);
//    }
//
//    /**
//     * 从客户端接收消息
//     * @param session
//     * @param message
//     */
//    @OnMessage
//    public void onMessage(Session session,String message){
//        // 客户端的消息json
//        JSONObject jsonObject = JSONObject.parseObject(message);
//        // 获取这条消息要发给谁
//        String to = jsonObject.getString("to");
//        Server webSocketServer = clients.get(to);
//        if(webSocketServer != null){
//            // 发送给客户端的消息
//            webSocketServer.session.getAsyncRemote().sendText(message);
//        }else{
//
//            session.getAsyncRemote().sendText(jsonObject.toJSONString("11"));
//        }
//    }
//
//    @OnClose
//    public void onClose(Session session){
//        downLine(session,null);
//    }
//
//    /**
//     * error参数必给,否则application run fail
//     * @param session
//     * @param error
//     */
//    @OnError
//    public void onError(Session session,Throwable error){
//        downLine(session,error);
//    }
//
//    private void downLine(Session session,Throwable error){
//        String clientName = sessionMap.get(session.getId());
//        clients.remove(clientName);
//        sessionMap.remove(session.getId());
//        int i = onlineTotal.decrementAndGet();
//        if(null != error){
//            log.info("客户端:{}发生异常:{},当前在线数:{}",clientName,error.getMessage(),i);
//        }else{
//            log.info("客户端:{}下线,当前在线数:{}",clientName,i);
//        }
//
//    }
//
//}
