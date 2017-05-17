package com.vteba.opencv.id;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.IOUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yinlei on 17-4-20.
 */
public class CreateTrainImage {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        String path = "/home/yinlei/sfz/all-hanzi/";
        Map<String, Integer> map = new HashMap<>();
        int i = 0;
        int k = 1;
        File files = new File(path);
        for (File file : files.listFiles()) {

            if (file.isFile()) {
                try {
                    String name = file.getName();

//                    name = name.substring(name.indexOf("_"));
//                    file.renameTo(new File("/home/yinlei/sfz/sfz_gray/yinlei_" + k++ + ".png"));

                    name = name.substring(name.indexOf("_") + 1, name.indexOf("."));
                    if (!map.containsKey(name)) {
                        map.put(name, i++);
                    }


//                    Mat image = Imgcodecs.imread(path + name);
//
//                    Mat resizeMat = new Mat();
//                    Size resize = new Size(36, 36);
//                    Imgproc.resize(image, resizeMat, resize);
//
//                    Mat dst = new Mat();
//                    Size size = new Size(3, 3); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
//                    Imgproc.blur(resizeMat, dst, size); // 平滑，降噪
//
//                    Mat gray = new Mat();
//                    Imgproc.cvtColor(dst, gray, Imgproc.COLOR_RGB2GRAY); // 灰度图
//
//                    Mat binary = new Mat(); // 二值化
//                    Imgproc.adaptiveThreshold(gray, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 13, 10);
//
//                    Imgcodecs.imwrite("/home/yinlei/sfz/all-hanzi/" + name, binary);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        int charNumber = map.size();
        System.out.println(charNumber);
        String json = JSON.toJSONString(map);
        File file = new File("/home/yinlei/" + charNumber + "zi.json");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(json.getBytes());
            fos.flush();
            IOUtils.closeQuietly(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
