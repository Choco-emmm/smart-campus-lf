package com.choco.smartlf.utils;

import java.util.UUID;

public class ImageNameUtil {

    public static String getImageName(String originalFileName){
        //获取文件的后缀
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        //生成新的文件名
        return UUID.randomUUID().toString().replaceAll("-", "") + extension;
    }
}
