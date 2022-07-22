package com.haruhi.bot.thread;


import com.haruhi.bot.constant.WebSocketConstant;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReConnectTask implements Runnable {

    private static long sleep = 2 * 1000;

    @Override
    public void run() {
        while (true){
            if(Client.getInstance() != null){
                log.info("连接成功:{}", WebSocketConstant.server_url);
                break;
            }else{
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void execute(){
        new Thread(new ReConnectTask()).start();
    }

}
