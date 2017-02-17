package com.vteba.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

public class SplitPic {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image = Imgcodecs.imread("/home/yinlei/yinlei.png");
        //System.out.println( "mat = " + image.dump() );

        int m = 2;
        int n = 2;

        int height = image.rows(); // 图片高度
        int width = image.cols(); // 图片宽度
        System.out.println("height:" + height + " width:" + width);
        int ceil_height = height / m;
        int ceil_width = width / n;
        System.out.println("ceil_height:" + ceil_height + " ceil_width:" + ceil_width);

        String filename = "/home/yinlei/cc_sub";

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                int a = j * ceil_width;
                int b = i * ceil_height;
                System.out.println(a + "," + b + "," + ceil_width + "," + ceil_height);
                Rect rect = new Rect(j * ceil_width, i * ceil_height, ceil_width, ceil_height);
                Mat roi_img = new Mat(image, rect);
                Mat tmp_img = new Mat();

                roi_img.copyTo(tmp_img);

                Imgcodecs.imwrite(filename + i + "_" + j + ".jpg", tmp_img);
            }
        }
    }
}
