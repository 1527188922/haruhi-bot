package com.haruhi.bot;

import com.haruhi.bot.job.schedule.JobManage;
import com.haruhi.bot.service.DataBaseService;
import com.haruhi.bot.thread.FirstTask;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SystemCommandLineRunner implements CommandLineRunner {

    @Autowired
    private DataBaseService dataBaseService;
    @Autowired
    private FirstTask firstTask;
    @Autowired
    private JobManage jobManage;

    @Override
    public void run(String... args) throws Exception {
        // 初始化数据库
        dataBaseService.dataBaseInit();
        log.info("开始连接go-cqhttp...");
        if(!Client.connect()){
            Client.reConnection();
        }
        // 执行项目首次启动需要执行的任务 比如从库里加载一些数据到内存
        firstTask.execute(firstTask);
        // 开启job
        jobManage.startAllJob();
    }
}
