package com.haruhi.bot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BotConfig {
    public static String SUPER_USER = "";
    public static String NAME = "";
    public static String SEARCH_IMAGE_KEY = "";

    @Autowired
    public void setSuperUser(@Value("${bot.super-user}") String superUser) {
        SUPER_USER = superUser;
    }
    @Autowired
    public void setName(@Value("${bot.name}") String name) {
        NAME = name;
    }
    @Autowired
    public void setSearchImageKey(@Value("${bot.search-image-key}") String searchImageKey){
        SEARCH_IMAGE_KEY = searchImageKey;
    }
}
