package com.haruhi.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

@Slf4j
@SpringBootApplication
public class HaruhiBotRun {
    public static void main(String[] args) {
        SpringApplication.run(HaruhiBotRun.class,args);
    }

    @PreDestroy
    public void preDestroy() {
        log.error("haruhi bot已停止运行！");
    }
}
