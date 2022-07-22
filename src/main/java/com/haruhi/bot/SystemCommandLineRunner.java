package com.haruhi.bot;

import com.haruhi.bot.handlers.command.AbstractCommandHandler;
import com.haruhi.bot.handlers.command.Subject;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SystemCommandLineRunner implements CommandLineRunner {

    @Autowired
    private Map<String, AbstractCommandHandler> handlerMap;

    @Override
    public void run(String... args) throws Exception {

        log.info("开始将命令处理类加载到集合...");
        for (AbstractCommandHandler value : handlerMap.values()) {
            Subject.attach(value);
        }
        log.info("加载了{}个命令处理类",handlerMap.size());
        log.info("开始连接go-cqhttp...");
        Client instance = Client.getInstance();
        if(instance == null){
            Client.reConnection();
        }else{
            log.info("连接go-cqhttp成功");
        }
    }
}
