package com.haruhi.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

/**
 * 功能/命令文档
 * https://www.yuque.com/liufang-bx5f9/rt4lg6/yfzt2g
 */
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
