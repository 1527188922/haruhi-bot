package com.haruhi.bot.handlers.command;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.factory.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractCommandHandler implements Runnable{
    /**
     * 优先
     * 正则匹配
     * 若该方法返回null,表示则走自定义匹配customMatches()
     * @return
     */
    protected abstract RegexEnum getRegex();

    /**
     * 该方法获取子类对象
     * 通过子类的有参构造
     * @param json
     * @return
     */
    protected abstract AbstractCommandHandler getSubclass(JSONObject json);

    /**
     * 自定义匹配
     * 若getRegex()返回null
     * 则通过这个方法进行匹配
     * @param json
     * @return
     */
    protected abstract boolean customMatches(JSONObject json,String command);

    /**
     * 当主题更新时,所有订阅者会调用该方法
     * @param json
     * @param command
     */
    public void matches(JSONObject json,String command) {

        boolean execute = false;
        if(getRegex() != null){
            if(command.matches(getRegex().getValue())){
                execute = true;
            }
        }else{
            execute = customMatches(json,command);
        }
        if(execute){
            log.info("匹配到命令:{}",command);
            ThreadPoolFactory.getCommandHandlerThreadPool().execute(getSubclass(json));
        }
    }
}
