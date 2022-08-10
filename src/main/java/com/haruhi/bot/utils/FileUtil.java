package com.haruhi.bot.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
@Slf4j
public class FileUtil {
    private FileUtil(){}

    public static void deleteFile(String path){
        deleteFile(new File(path));
    }
    public static void deleteFile(File file){
        if(file.exists()){
            file.delete();
        }
    }
}
