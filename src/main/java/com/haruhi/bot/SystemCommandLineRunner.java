package com.haruhi.bot;

import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SystemCommandLineRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        log.info("开始连接go-cqhttp...");
        Client instance = Client.getInstance();
        if(instance == null){
            Client.reConnection();
        }else{
            log.info("连接go-cqhttp成功");
        }
    }
}
