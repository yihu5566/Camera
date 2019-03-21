package com.example.lxrent.camerademo.util;

import com.zero.smallvideorecord.LocalMediaCompress;
import com.zero.smallvideorecord.model.AutoVBRMode;
import com.zero.smallvideorecord.model.LocalMediaConfig;
import com.zero.smallvideorecord.model.OnlyCompressOverBean;

/**
 * @Author : dongfang
 * @Created Time : 2019-03-20  17:52
 * @Description:
 */
public class CompressVideoUtils {


    public CompressVideoUtils() {
    }

    /**
     * 压缩文件哦
     *
     * @param filePath
     * @return
     */
    public static String startCompress(String filePath) {
        //初始化压缩
        LocalMediaConfig.Buidler buidler = new LocalMediaConfig.Buidler();
        final LocalMediaConfig config = buidler
                .setVideoPath(filePath)
                .captureThumbnailsTime(1)
                .doH264Compress(new AutoVBRMode())
                .setFramerate(10)
                .build();
        OnlyCompressOverBean onlyCompressOverBean = new LocalMediaCompress(config).startCompress();
        if (onlyCompressOverBean.isSucceed()) {
            LogUtil.d(onlyCompressOverBean.getVideoPath());
        }
        return onlyCompressOverBean.getVideoPath();

    }


}
