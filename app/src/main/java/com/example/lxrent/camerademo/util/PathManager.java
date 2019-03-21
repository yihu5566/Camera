package com.example.lxrent.camerademo.util;

import java.io.File;

/**
 * Created by sreay on 14-8-18.
 */
public class PathManager {
    public static String paraPhotoPath = "/aaaa/photo/";
    public static String paraVideoPath = "/aaaa/video/";
    public static String paraVideoPathTemp = "/aaaa/video/temp";


    //自定义相机存储路径（图片经过剪裁后的图片，生成640*640）
    public static File getCropPhotoPath() {
        File photoFile = new File(getCropPhotoDir().getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpeg");
        return photoFile;
    }

    public static File getCropVideoPath() {
        File photoFile = new File(getCropVideoDir().getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp4");
        return photoFile;
    }

    public static File getCropVideoTempPath() {
        File photoFile = new File(getCropVideoTempDir() + "/" + System.currentTimeMillis() + ".mp4");
        return photoFile;
    }

    //存储剪裁后的图片的文件夹
    public static File getCropPhotoDir() {
        String path = FileUtil.getRootPath() + paraPhotoPath;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    //存储剪裁后的图片的文件夹
    public static File getCropVideoDir() {
        String path = FileUtil.getRootPath() + paraVideoPath;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    //存储剪裁后的图片的文件夹
    public static String getCropVideoTempDir() {
        String path = FileUtil.getRootPath() + paraVideoPathTemp;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }
}
