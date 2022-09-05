package com.haruhi.bot.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FileUtil {
    private FileUtil(){}

    public static void deleteFile(String path){
        if(Strings.isNotBlank(path)){
            deleteFile(new File(path));
        }
    }
    public static void deleteFile(File file){
        if(file.exists()){
            file.delete();
        }
    }
    public static File[] getAllFileList(String path){
        return getAllFileList(new File(path));
    }
    public static List<File> getFileList(String path){
        return getFileList(new File(path));
    }
    public static List<File> getDirectoryList(String path){
        return getDirectoryList(new File(path));
    }
    public static List<File> getDirectoryList(File file){
        File[] allFileList = getAllFileList(file);
        ArrayList<File> res = new ArrayList<>(allFileList.length);
        for (File file1 : allFileList) {
            if(file1.isDirectory()){
                res.add(file1);
            }
        }
        return res;
    }
    public static List<File> getFileList(File file){
        File[] allFileList = getAllFileList(file);
        ArrayList<File> res = new ArrayList<>(allFileList.length);
        for (File file1 : allFileList) {
            if(file1.isFile()){
                res.add(file1);
            }
        }
        return res;
    }
    public static File[] getAllFileList(File file){
        if(file.exists() && file.isDirectory()){
            return file.listFiles();
        }
        return null;
    }
}
