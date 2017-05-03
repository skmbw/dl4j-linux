package com.vteba.opencv.id;

import com.vteba.opencv.OpenCVUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by yinlei on 17-4-25.
 */
public class ResizeTestImage {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static String s = "38";

    public static void main(String[] args) {
//        Mat image = Imgcodecs.imread("/home/yinlei/address2.png");
//        OpenCVUtils.splitBinaryAddress(image);

        sliceAddress();


//        Mat image = Imgcodecs.imread("/tmp/22.png", 0);
//        Mat dst = new Mat(36, 36, CvType.CV_8UC1);
//
//        Size size = new Size(33, 36); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
//
//        Rect rect = new Rect(0, 0, 33, 36);
//        Mat gray = new Mat(image, rect);
////        Imgcodecs.imwrite("/tmp/211_2.png", gray);
////        gray.put(0, 33, 0);
//
//        gray.copyTo(dst);
//
//
//        Size size2 = new Size(36, 36);
//        Imgproc.resize(dst, dst, size2);
//
//        Imgcodecs.imwrite("/tmp/22_1.png", dst);


    }

    public static void sliceAddress() {
        String path = "/home/yinlei/sfz_test/";
        File files = new File(path);
        for (File file : files.listFiles()) {
            if (file.isFile()) {
                String name = file.getName();
                if (name != null && (name.endsWith(".jpg") || name.endsWith(".png"))) {
                    try {
                        Mat image = Imgcodecs.imread(path + name, 0);
                        image = OpenCVUtils.resize(image, 420);
                        Map<Integer, List<Mat>> charListMap = OpenCVUtils.splitAddressChar(image);

                        int row = charListMap.size();
                        for (int i = 1; i <= row; i++) {
                            List<Mat> charList = charListMap.get(i);
                            int j = 1;
                            for (Mat charMat : charList) {
                                Imgcodecs.imwrite("/home/yinlei/sfz/sfz_gray/" + System.currentTimeMillis() + i + j++ + ".png", charMat);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                if (file.length() == 0) {
//                    file.delete();
//                }

            }
        }
    }
}
