package com.haruhi.bot.handlers.event;

import com.haruhi.bot.constant.RegexEnum;

/**
 * 用来定义java bean类型
 */
public interface IMessageEventType {
    RegexEnum getRegex();
}
